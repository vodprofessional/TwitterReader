package com.vodprofessionals.socialexplorer.model

import _root_.java.util.Calendar

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.persistence.{ContextAwareRDBMSProfile, SlickComponents}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery => Q}
import scala.collection.JavaConverters._




/**
 *
 */
class Reports(val dbProfile: JdbcProfile) extends SlickComponents with ContextAwareRDBMSProfile with LazyLogging {
  import dbProfile.simple._


  def numTweets(): java.util.List[java.util.List[Object]] = DB withSession { implicit session: Session =>
    val r = Q.queryNA[(String, String, Long)](
    """
      SELECT
        term,
        CONCAT(DATE(tweetedAt), " ", EXTRACT(HOUR FROM tweetedAt) DIV 6) sixhourgroup,
        COUNT(id)
      FROM
        tweets
      GROUP BY
        sixhourgroup,
        term
      ORDER BY
        sixhourgroup,
        term;
    """
    ).list

    def proc( curr: Map[String, Map[String, Long]], item: (String, String, Long) ) = {
      curr + ( item._1 -> ( curr.getOrElse(item._1, Map.empty) + (item._2 -> item._3) ) )
    }

    val rawInput = r.foldLeft[Map[String, Map[String, Long]]](Map.empty)(proc)

    // Generate calendar
    val cal = Calendar.getInstance
    val today = cal.clone.asInstanceOf[Calendar]

    def calstep(): List[String] =
      if (today.compareTo(cal) < 0)
        List()
      else {
        cal.add(Calendar.HOUR_OF_DAY, 6)
        cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+
          " "+(cal.get(Calendar.HOUR_OF_DAY) / 6) :: calstep()
      }

    cal.add(Calendar.MONTH, -3)
    val calendar = calstep()

    val tableHeader: java.util.List[Object] = ("".asInstanceOf[Object] :: (for(calItem <- calendar) yield calItem.asInstanceOf[Object]).toList).asJava
    // Process data into lists
    ( tableHeader ::
        (for((itemName, item) <- rawInput) yield {
          (itemName :: (for (calItem <- calendar) yield {
           item.getOrElse(calItem, 0).asInstanceOf[Object]
          }).toList).asJava
        }).toList
    ).asJava
  }
}
