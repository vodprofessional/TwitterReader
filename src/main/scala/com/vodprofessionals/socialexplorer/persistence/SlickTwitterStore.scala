package com.vodprofessionals.socialexplorer.persistence

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.Tweet
import hu.lazycat.scala.config.Configurable
import hu.lazycat.scala.immutable.Int
import hu.lazycat.scala.slick.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile}
import scala.slick.driver.JdbcProfile


case class AddTwitterSearchTerm(term: String)
case class AddTwitterMessage(tweet: Tweet)


/**
 *
 */
class SlickTwitterStore (
        override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.driver
    ) extends Actor
      with SlickComponents
      with ContextAwareRDBMSProfile
      with LazyLogging
      with Configurable {

  import dbProfile.simple._


  val tweets = TableQuery[Tweets]

  /**
   *
   * @return
   */
  def receive = {
    case AddTwitterMessage(tweet: Tweet) =>
      DB withSession { implicit session: Session =>
        try {
          tweets += tweet
        } catch {
          case sqlException: java.sql.SQLException =>
            sqlException.getSQLState match {
              case Int(sqlStateCode) => sqlStateCode match {
                case code if 0x23000 until 0x23FFF contains code => {
                  /* This is an integrity violation exception, just ignore it,
                   most likely duplicate key
                   http://www.pitt.edu/~hoffman/oradoc/server.804/a58231/appd.htm */
                }
                case _ => logger.error("Database Error: " + sqlException.getMessage)
              }
              case _ => logger.error("Database Error: " + sqlException.getMessage)
            }
          case exception: Exception =>
            logger.error(exception.getMessage, exception)
        }
      }

    case AddTwitterSearchTerm(term) => {
      // TODO: Implement case AddSearchTerm
    }
  }

}
