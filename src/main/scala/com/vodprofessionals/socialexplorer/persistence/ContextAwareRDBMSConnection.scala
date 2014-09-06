package com.vodprofessionals.socialexplorer.persistence

import scala.slick.driver._
import com.vodprofessionals.socialexplorer.config.Configurable

/**
 *
 */
trait ContextAwareRDBMSProfile {
  val dbProfile: JdbcProfile
}

/**
 *
 */
object ContextAwareRDBMSDriver extends Configurable {

  /**
   * Provides the Slick driver for queries and operations
   */
  val driver = {
    CONFIG.getString("database.driver") match {
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
                                   ) extends ContextAwareRDBMSProfile with Configurable {

  import dbProfile.simple._

  /**
   * Returns a @Database object to configure Slick
   *
   * @return Database The instance
   */
  def databaseObject: Database = {
    val url = CONFIG.getString("database.url")
    val username = CONFIG.getString("database.username")
    val password = CONFIG.getString("database.password")
    val driver = CONFIG.getString("database.driver")

    Database.forURL(url, username, password, null, driver)
  }
}
