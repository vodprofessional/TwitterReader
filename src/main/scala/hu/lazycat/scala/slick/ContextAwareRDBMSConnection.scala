package hu.lazycat.scala.slick

import scala.slick.driver._
import hu.lazycat.scala.config.AppConfig
import com.typesafe.scalalogging.slf4j.Logging

/**
 *
 */
trait ContextAwareRDBMSProfile {
  val dbProfile: JdbcProfile
}

/**
 *
 */
object ContextAwareRDBMSDriver {

  /**
   *
   */
  def getDriver = {
    AppConfig.config.getString("db.driver") match {
      case "com.mysql.jdbc.Driver"  => MySQLDriver
      case "org.h2.Driver"          => H2Driver
      case "org.postgresql.Driver"  => PostgresDriver
      case _                        => JdbcDriver
    }
  }
}

/**
 *
 * @param dbProfile
 */
class ContextAwareRDBMSConnection(
                  override val dbProfile: JdbcProfile
         ) extends ContextAwareRDBMSProfile with Logging {

  import dbProfile.simple._

  /**
   * Returns a @Database object to configure Slick
   *
   * @return Database The instance
   */
  def databaseObject: Database = {
    val url = AppConfig.config.getString("db.url")
    val username = AppConfig.config.getString("db.username")
    val password = AppConfig.config.getString("db.password")
    val driver = AppConfig.config.getString("db.driver")
    logger.info("Connection info =>" + "Run mode: " + AppConfig.env + ", db url: " + url + ", driver: " + driver)
    Database.forURL(url, username, password, null, driver)
  }
}
