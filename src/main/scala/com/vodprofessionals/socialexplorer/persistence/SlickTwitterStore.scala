package com.vodprofessionals.socialexplorer.persistence

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.config.Configurable
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.scala.Int
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
        tweets.insertOrUpdate(tweet)
      } catch {
        case sqlException: java.sql.SQLException =>
          sqlException.getSQLState match {
            case Int(sqlStateCode) => sqlStateCode match {
              case 23000 => {
                logger.error("Database Error (Integrity Violation): " + sqlException.getMessage)
              }
              case _ => logger.error("Database Error: " + sqlException.getMessage + " SQLSTATE: " + sqlStateCode)
            }
            case sqlStateCode: String => sqlException.getErrorCode match {
              case 1366 => {
                logger.warn("Cannot convert tweet text to MySQL UTF8")
              }
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
        tweeters.insertOrUpdate(tweeter)
      } catch {
        case sqlException: java.sql.SQLException =>
          sqlException.getSQLState match {
            case Int(sqlStateCode) => sqlStateCode match {
              case code if 23000 == code => {
                logger.error("Database Error (Integrity Violation): " + sqlException.getMessage)
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
