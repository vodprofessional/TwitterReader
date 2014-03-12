package com.vodprofessionals.socialexplorer

import hu.lazycat.scala.slick._
import scala.slick.driver.JdbcProfile
import com.typesafe.scalalogging.slf4j.Logging
import java.sql.Date

class Worker(
              override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.getDriver
      ) extends DomainComponent with ContextAwareRDBMSProfile with Logging {

  import dbProfile.simple._

  val DB = new ContextAwareRDBMSConnection(dbProfile)

  /**
   *
   */
  def start = {
    DB.databaseObject withSession { implicit session: Session =>
      try {
        tweets.ddl.create
      } catch {
        case ex: Exception => logger.error(ex.getMessage)
      }

      val today = new java.util.Date()
      tweets += Tweet(
        "This is the text message, the tweet for the term",
        "Tweeter is tolmi",
        "for the term",
        new Date(today.getTime)
      )

      tweets foreach println
    }
  }

}
