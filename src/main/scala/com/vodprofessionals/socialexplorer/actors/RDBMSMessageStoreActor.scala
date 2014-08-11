package com.vodprofessionals.socialexplorer.actors

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{Tweet, Tweets}
import com.vodprofessionals.socialexplorer.persistence.SlickComponents
import hu.lazycat.scala.config.Configurable
import hu.lazycat.scala.immutable.Int
import hu.lazycat.scala.slick.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile}
import scala.slick.driver.JdbcProfile


case class TwitterMessage(content: String)

case class AddSearchTerm(term: String)


/**
 *
 */
class RDBMSMessageStoreActor(
        override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.driver
    ) extends Actor with SlickComponents with ContextAwareRDBMSProfile with LazyLogging with Configurable {

  import dbProfile.simple._

  def receive = {
    case TwitterMessage(content) =>
      try {
        val tweet: Tweet = Tweets.fromJSON(content)(List())
        if (tweet.term.length > 0) {
          TWEETS += tweet
        }
        else {
          logger.info("Ignoring tweet as no search term detected: " + content)
        }
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
    case AddSearchTerm(term) => {

    }
  }

}
