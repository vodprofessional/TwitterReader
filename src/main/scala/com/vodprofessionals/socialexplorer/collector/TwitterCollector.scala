package com.vodprofessionals.socialexplorer.collector

import com.typesafe.scalalogging.LazyLogging
import hu.lazycat.scala.config.Configurable
import hu.lazycat.scala.immutable.Int
import hu.lazycat.scala.slick._
import scala.slick.driver.JdbcProfile
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.core.{Constants, Client}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import java.sql.SQLSyntaxErrorException
import scala.collection.JavaConverters._
import org.postgresql.util.PSQLException
import com.vodprofessionals.socialexplorer.DomainComponent


class TwitterCollector(
              override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.driver
      ) extends DomainComponent
        with Collector
        with ContextAwareRDBMSProfile
        with LazyLogging
        with Configurable {

  import dbProfile.simple._

  val DB = new ContextAwareRDBMSConnection(dbProfile)
  val QUEUE: BlockingQueue[String] = new LinkedBlockingQueue[String](10000)
  val ENDPOINT = new StatusesFilterEndpoint
  val twitter: Client = (new ClientBuilder).hosts(Constants.STREAM_HOST)
                                           .endpoint(ENDPOINT)
                                           .authentication(new OAuth1(CONFIG.getString("twitter.consumer.key"),
                                                                      CONFIG.getString("twitter.consumer.secret"),
                                                                      CONFIG.getString("twitter.token.key"),
                                                                      CONFIG.getString("twitter.token.secret")))
                                           .processor(new StringDelimitedProcessor(QUEUE))
                                           .build

  /**
   *
   */
  override def start = {
    DB.databaseObject withSession { implicit session: Session =>

      try {
        tweets.ddl.create
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

      val terms = CONFIG.getStringList("temp.terms")
      ENDPOINT.trackTerms(terms)

      // Register shutdown hook to terminate the Twitter hose
      sys addShutdownHook {
        stop
      }

      twitter.connect

      while (!twitter.isDone) {
        try {
          val t = Tweets.fromJSON(QUEUE.take)(terms.asScala.toList);
          if (t.term.length > 0) {
            tweets += t
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

  /**
   * Shuts down the worker application
   *
   */
  override def stop = {
    logger.info("Shutting down...")

    twitter.stop()

    logger.info("Shutdown complete")
  }
}
