package com.vodprofessionals.socialexplorer.persistence

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import hu.lazycat.scala.config.Configurable
import hu.lazycat.scala.immutable.Int
import hu.lazycat.scala.slick.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile}
import scala.slick.driver.JdbcProfile



/**
 *
 */
class SlickTwitterStore (
        override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.driver
    ) extends TwitterStore
      with SlickComponents
      with ContextAwareRDBMSProfile
      with LazyLogging
      with Configurable {

  import dbProfile.simple._

  val tweets = TableQuery[Tweets]
  val tweeters = TableQuery[Tweeters]


  /**
   *
   * @param tweet
   * @return
   */
  def insertTweet(tweet: Tweet) =
    DB withSession { implicit session: Session =>
      try {
        if (tweet.retweets > 0)
          session.withTransaction {
            if (tweets.filter(_.tweetId === tweet.tweetId).length.run > 0) {
              tweets.filter(_.tweetId === tweet.tweetId).update(tweet)
              logger.info("Got One Existing Retweet")
            }
            else {
              tweets += tweet
              logger.info("Got One New Retweet")
            }
          }
        else
          tweets += tweet
      } catch {
        case sqlException: java.sql.SQLException =>
          sqlException.getSQLState match {
            case Int(sqlStateCode) => sqlStateCode match {
              case 23000 => {
                /* This is an integrity violation exception, just ignore it,
                 most likely duplicate key
                 http://www.pitt.edu/~hoffman/oradoc/server.804/a58231/appd.htm */
              }
              case _ => logger.error("Database Error: " + sqlException.getMessage + " SQLSTATE: " + sqlStateCode)
            }
            case sqlStateCode: String => sqlException.getErrorCode match {
              case 1366 => logger.warn("Cannot convert tweet text to MySQl UTF8")
              case _ => logger.error("Database Error: " + sqlException.getMessage + " ERRORCODE: " + sqlException.getErrorCode)
            }
            case _ => logger.error("Database Error: " + sqlException.getMessage + " ERRORCODE: " + sqlException.getErrorCode)
          }
        case exception: Exception =>
          logger.error(exception.getMessage, exception)
      }
    }

  /**
   *
   * @param tweeter
   */
  def insertTweeter(tweeter: Tweeter) =
    DB withSession { implicit session: Session =>
      try {
        tweeters += tweeter
      } catch {
        case sqlException: java.sql.SQLException =>
          sqlException.getSQLState match {
            case Int(sqlStateCode) => sqlStateCode match {
              case code if 23000 == code => {
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

}
