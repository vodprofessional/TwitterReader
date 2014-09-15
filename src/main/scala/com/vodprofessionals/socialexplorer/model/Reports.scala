package com.vodprofessionals.socialexplorer.model

import _root_.java.sql.Date
import _root_.java.text.SimpleDateFormat
import _root_.java.util.Calendar

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.persistence.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile, SlickComponents}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery => Q}
import scala.collection.JavaConverters._
import scala.collection.immutable.StringOps




/**
 *
 */
class Reports(val dbProfile: JdbcProfile) extends SlickComponents with ContextAwareRDBMSProfile with LazyLogging {
  import dbProfile.simple._


  def numTweets(amount: Int, pieces: Int): java.util.List[java.util.List[Object]] = DB withSession { implicit session: Session =>
    val cal = Calendar.getInstance
    val today = cal.clone.asInstanceOf[Calendar]
    cal.add(pieces, amount)

    val sql = ContextAwareRDBMSDriver.driver.toString match {
      case "MySQLDriver" =>    """
                                SELECT
                                  term,
                                  CONCAT(DATE(tweetedAt), " ", EXTRACT(HOUR FROM tweetedAt) DIV 6) sixhourgroup,
                                  COUNT(id)
                                FROM
                                  tweets
                                WHERE
                                  tweetedAt > ?
                                GROUP BY
                                  sixhourgroup,
                                  term
                                ORDER BY
                                  sixhourgroup,
                                  term;
                               """
      case "PostgresDriver" => """
                                SELECT
                                  term,
                                  CONCAT(DATE("tweetedAt"), ' ', ROUND(EXTRACT(HOUR FROM "tweetedAt") / 6)) sixhourgroup,
                                  COUNT(id)
                                FROM
                                  tweets
                                WHERE
                                  "tweetedAt" > ?
                                GROUP BY
                                  sixhourgroup,
                                  term
                                ORDER BY
                                  sixhourgroup,
                                  term;
                               """
      case _ =>                """
                                SELECT
                                  "Not Supported",
                                  "" sixhourgroup,
                                  COUNT(id)
                                FROM
                                  tweets
                                WHERE
                                  "tweetedAt" > ?
                                GROUP BY
                                  sixhourgroup,
                                  term
                                ORDER BY
                                  sixhourgroup,
                                  term;
                               """
    }

    val q = Q.query[Date, (String, String, Long)](sql)

    val r = q(new Date(cal.getTime.getTime)).list

    def proc( curr: Map[String, Map[String, Long]], item: (String, String, Long) ) = {
      curr + ( item._1 -> ( curr.getOrElse(item._1, Map.empty) + (item._2 -> item._3) ) )
    }

    val rawInput = r.foldLeft[Map[String, Map[String, Long]]](Map.empty)(proc)

    // Generate calendar

    def padTo(item: String, len: Int, value: String): String = {
      if (item.length < len)
        padTo(value + item, len-1, value)
      else
        item
    }

    def calstep(): List[String] =
      if (today.compareTo(cal) < 0)
        List()
      else {
        cal.add(Calendar.HOUR_OF_DAY, 6)
        cal.get(Calendar.YEAR) + "-" +
          padTo((cal.get(Calendar.MONTH) + 1).toString, 2, "0") + "-" +
          padTo(cal.get(Calendar.DAY_OF_MONTH).toString, 2, "0") +
          " "+(cal.get(Calendar.HOUR_OF_DAY) / 6) :: calstep()
      }

    val calendar = calstep()

    val tableHeader: java.util.List[Object] = (
      "".asInstanceOf[Object] ::
      (for(itemName <- rawInput.keys) yield itemName.asInstanceOf[Object]).toList
    ).asJava

    (tableHeader ::
      (for(calItem <- calendar) yield {
        (calItem ::
          (for((name, value) <- rawInput) yield {
            value.getOrElse(calItem, 0).asInstanceOf[Object]
          }).toList
        ).asJava
      }).toList
    ).asJava
  }
}
