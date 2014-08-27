package com.vodprofessionals.socialexplorer

import _root_.akka.actor.{ActorSystem, Props}
import _root_.akka.routing.RoundRobinGroup
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.RDBMSTwitterStoreMessages.AddTwitterMessage
import com.vodprofessionals.socialexplorer.akka.TwitterCollectorMessages.StartTwitterCollector
import com.vodprofessionals.socialexplorer.akka._
import com.vodprofessionals.socialexplorer.domain.{Tweet, RawTweet}
import com.vodprofessionals.socialexplorer.persistence.{SlickComponents, SlickTwitterStore}
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import com.vodprofessionals.socialexplorer.web.{StartVaadinService, VaadinService}
import hu.lazycat.scala.config.Configurable
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import hu.lazycat.scala.slick.{ContextAwareRDBMSProfile, ContextAwareRDBMSDriver}

import scala.slick.driver.JdbcProfile


/**
 *
 */
object Application extends App with LazyLogging with Configurable {
  val actorSystem    = ActorSystem("socialStreamNodeSystem")
  val processorCores = Runtime.getRuntime().availableProcessors()
  val nrOfWorkers    = if (processorCores > 1) processorCores * 2 - 1 else 1

  try {
    if (args.length > 0)
      actorSystem.actorOf(Props[VaadinService]) ! StartVaadinService  // Looks like we are a web node

    //runWithAkka
    runPlain

  } catch {
    case ex: Throwable => logger.error(ex.getMessage, ex)
  }


  /**
   *
   */
  def runPlain = {
    val slickTwitterStore = new SlickTwitterStore();
    val twitterProcessor = new TwitterProcessor(
      (tweet: Tweet) => slickTwitterStore.insert(tweet)
    )
    val twitterCollector = new TwitterCollector(
      CONFIG.getString("twitter.consumer.key"),
      CONFIG.getString("twitter.consumer.secret"),
      CONFIG.getString("twitter.token.key"),
      CONFIG.getString("twitter.token.secret"),
      (rawTweet: RawTweet) => {
        twitterProcessor.process(rawTweet)
        true
      }
    )

    // Let's load the search terms from the DB
    class DAL(val dbProfile: JdbcProfile) extends SlickComponents with ContextAwareRDBMSProfile {
      import dbProfile.simple._

      DB withSession { implicit session: Session =>
        createTables(session)
      }

      def getSearchTerms(): List[String] =
        DB withSession { implicit session: Session =>
          val searchTerms = TableQuery[SearchTerms]
          (for(t <- searchTerms) yield t.term).run
            .toList
        }
    }
    val terms = (new DAL(ContextAwareRDBMSDriver.driver)).getSearchTerms

    twitterCollector.start(terms)
  }

  /**
   *
   */
  def runWithAkka = {
    // Attempt to create the Slick RDBMS twitter store
    val slickStoreActor = actorSystem.actorOf(Props(new RDBMSTwitterStoreActor(new SlickTwitterStore())))

    // Attempt to boot up the Twitter processor actors
    val actorPaths = for (i <- 1 to nrOfWorkers)
    yield actorSystem.actorOf(Props(new TwitterProcessorActor(new TwitterProcessor(
        (tweet: Tweet) => slickStoreActor ! AddTwitterMessage(tweet)
      )))).path.toString

    val twitterProcessorActors = actorSystem.actorOf(RoundRobinGroup(actorPaths).props(), "twitterProcessorPool")


    // Let's load the search terms from the DB
    class DAL(val dbProfile: JdbcProfile) extends SlickComponents with ContextAwareRDBMSProfile {
      import dbProfile.simple._

      DB withSession { implicit session: Session =>
        createTables(session)
      }

      def getSearchTerms(): List[String] =
        DB withSession { implicit session: Session =>
          val searchTerms = TableQuery[SearchTerms]
          (for(t <- searchTerms) yield t.term).run
            .toList
        }
    }
    val terms = (new DAL(ContextAwareRDBMSDriver.driver)).getSearchTerms

    // Attempt to start a Twitter collector actor
    val twitterCollector = new TwitterCollector(
      CONFIG.getString("twitter.consumer.key"),
      CONFIG.getString("twitter.consumer.secret"),
      CONFIG.getString("twitter.token.key"),
      CONFIG.getString("twitter.token.secret"),
      (rawTweet: RawTweet) => {
        twitterProcessorActors ! rawTweet; true
      })
    if(!terms.isEmpty)
      actorSystem.actorOf(Props(new TwitterCollectorActor(twitterCollector))) ! StartTwitterCollector( terms )

    else
      logger.error("No search terms defined so not starting collector")
  }
}
