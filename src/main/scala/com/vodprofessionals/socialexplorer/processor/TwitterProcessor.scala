package com.vodprofessionals.socialexplorer.processor

import _root_.java.sql.SQLSyntaxErrorException

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{RawTweet, Tweets}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import hu.lazycat.scala.immutable.Int
import com.vodprofessionals.socialexplorer.persistence.SlickComponents
import hu.lazycat.scala.slick.{ContextAwareRDBMSProfile, ContextAwareRDBMSDriver, ContextAwareRDBMSConnection}
import org.postgresql.util.PSQLException

import scala.slick.driver.JdbcProfile

/**
 *
 */
class TwitterProcessor(
                        override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.driver
                        ) extends Processor with LazyLogging with SlickComponents with ContextAwareRDBMSProfile with Actor {
  import dbProfile.simple._

  val DB = new ContextAwareRDBMSConnection(dbProfile)


  /*
   *
   */
  override def start() = {
    DB.databaseObject withSession { implicit session: Session =>
      try {
        TWEETS.ddl.create
      } catch {
        case ddlExists: SQLSyntaxErrorException => {
          logger.debug("DDL already set up, resuming...")
        }
        case ddlExists: PSQLException => {
          logger.debug("DDL already set up, resuming...")
        }
        case ex: Exception => {
          // Log everything else and terminate because at this point it's probably a connection issue
          // TODO: Poll for connection to database until established for a more reactive approach
          logger.error(ex.getMessage, ex)
        }
      }
    }

    // TODO Implement processor feeding
  }

  /*
   *
   */
  def receive = {
    case RawTweet(msg) => process(msg)
    case x:_ => logger.error("Message other than RawTweet received: " + x.toString)
  }

  /*
   *
   */
  def process(jsonMessage: String): Unit = {
    DB.databaseObject withSession { implicit session: Session =>
      try {
        val t = Tweets.fromJSON(jsonMessage, SearchTerms.matchTerms(jsonMessage));
        if (t.term.length > 0) {
          TWEETS += t
        }
      }
      catch {
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
  }

}
