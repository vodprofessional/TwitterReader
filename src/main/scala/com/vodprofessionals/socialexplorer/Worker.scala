package com.vodprofessionals.socialexplorer

import hu.lazycat.scala.slick._
import scala.slick.driver.JdbcProfile
import com.typesafe.scalalogging.slf4j.Logging
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}
import com.twitter.hbc.httpclient.auth.OAuth1
import com.twitter.hbc.core.{Constants, Client}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import java.sql.SQLSyntaxErrorException
import scala.collection.JavaConverters._
import org.postgresql.util.PSQLException
import hu.lazycat.scala.config.AppConfig


class Worker(
              override val dbProfile: JdbcProfile = ContextAwareRDBMSDriver.getDriver
      ) extends DomainComponent with ContextAwareRDBMSProfile with Logging {

  import dbProfile.simple._

  val DB = new ContextAwareRDBMSConnection(dbProfile)
  val QUEUE: BlockingQueue[String] = new LinkedBlockingQueue[String](10000)
  val ENDPOINT = new StatusesFilterEndpoint
  val TWITTER: Client = (new ClientBuilder).hosts(Constants.STREAM_HOST)
                                            .endpoint(ENDPOINT)
                                            .authentication(new OAuth1("LX7hoWly7G70JgYJ6PWi3A","MjTllvHJ0OOw7Zsd889Gk5ZR8TIqLbru4e2Pyph5Oo","2210803538-fNk5n6842w8tVQQ0l0xEA6LdrmV2uzmkb4yk5t0","0SK74hSwjOQqkzLrNvCy9mHs3Wh2J8Krm6SVUw2nUSsBz"))
                                            .processor(new StringDelimitedProcessor(QUEUE))
                                            .build

  /**
   *
   */
  def start = {
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

      val terms = AppConfig.config.getStringList("terms")
      ENDPOINT.trackTerms(terms)
      TWITTER.connect

      while(!TWITTER.isDone) {
        try {
          tweets += tweets.baseTableRow.parseFromJSON(QUEUE.take)(terms.asScala.toList)
        } catch {
          case sqlException: java.sql.SQLException => logger.error(sqlException.getMessage, sqlException)
        }
      }

    }
  }

  /**
   *
   *
   */
  def stop = {
    TWITTER.stop()
  }
}
