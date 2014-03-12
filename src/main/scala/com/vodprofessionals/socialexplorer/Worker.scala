package com.vodprofessionals.socialexplorer

import hu.lazycat.scala.slick._
import scala.slick.driver.JdbcProfile
import com.typesafe.scalalogging.slf4j.Logging
import java.sql.Date
import com.twitter.hbc.core.endpoint.{StatusesFilterEndpoint, StreamingEndpoint}
import com.google.common.collect.Lists
import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}
import com.twitter.hbc.httpclient.auth.{OAuth1, Authentication}
import com.twitter.hbc.core.{Constants, Client}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.processor.StringDelimitedProcessor

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
        case ex: Exception => logger.error(ex.getMessage)
      }

      ENDPOINT.trackTerms(Lists.newArrayList("lies"))
      TWITTER.connect


    /*

      val today = new java.util.Date()
      tweets += Tweet(
        "This is the text message, the tweet for the term",
        "Tweeter is tolmi",
        "for the term",
        new Date(today.getTime)
      )

      tweets foreach println

    */

    }
  }

  def stop = {

  }
}
