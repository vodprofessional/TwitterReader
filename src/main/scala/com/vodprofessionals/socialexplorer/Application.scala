package com.vodprofessionals.socialexplorer

import _root_.akka.actor.{ActorSystem, Props}
import _root_.akka.routing.RoundRobinGroup
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.RDBMSTwitterStoreMessages.AddTwitterMessage
import com.vodprofessionals.socialexplorer.akka.TwitterCollectorMessages.StartTwitterCollector
import com.vodprofessionals.socialexplorer.akka._
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import com.vodprofessionals.socialexplorer.persistence.{SlickComponents, SlickTwitterStore}
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import com.vodprofessionals.socialexplorer.web.{WebServer, StartVaadinService, VaadinService}
import hu.lazycat.scala.config.Configurable
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import hu.lazycat.scala.slick.{ContextAwareRDBMSProfile, ContextAwareRDBMSDriver}
import twitter4j.User

import scala.slick.driver.JdbcProfile


/**
 *
 */
object Application extends App with LazyLogging with Configurable {
  val actorSystem    = ActorSystem("socialStreamNodeSystem")
  val processorCores = Runtime.getRuntime().availableProcessors()
  val nrOfWorkers    = if (processorCores > 1) processorCores * 2 - 1 else 1

  try {
    //runWithAkka(Integer.parseInt(args.head))
    runPlain(Integer.parseInt(args.head))
  } catch {
    case ex: Throwable => logger.error(ex.getMessage, ex)
  }


  /**
   *
   */
  def runPlain(port: Int) = {
    val webServer = new WebServer
    if (port > 0)
      webServer.start(port)

    val slickTwitterStore = new SlickTwitterStore();

    val twitterProcessor = new TwitterProcessor(
      (tweet: Tweet, tweeter: Tweeter) => {
        slickTwitterStore.insertTweeter(tweeter)
        slickTwitterStore.insertTweet(tweet)
      }
    )

    val twitterCollector = new TwitterCollector(
      CONFIG.getString("twitter.consumer.key"),
      CONFIG.getString("twitter.consumer.secret"),
      CONFIG.getString("twitter.token.key"),
      CONFIG.getString("twitter.token.secret"),
      (rawTweet: String) => {
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

    if (!terms.isEmpty) {
      SearchTerms.addTerms(terms)
      twitterCollector.start
    } else {
      logger.warn("The search terms are empty. Please define at least one search term.")
    }
  }

  /**
   *
   */
  def runWithAkka(port: Int) = {
    if (port > 0)
      actorSystem.actorOf(Props[VaadinService]) ! StartVaadinService(port)

    // Attempt to create the Slick RDBMS twitter store
    val slickStoreActor = actorSystem.actorOf(Props(new RDBMSTwitterStoreActor(new SlickTwitterStore())))

    // Attempt to boot up the Twitter processor actors
    val actorPaths = for (i <- 1 to nrOfWorkers)
    yield actorSystem.actorOf(Props(new TwitterProcessorActor(new TwitterProcessor(
        (tweet: Tweet, tweeter: Tweeter) => slickStoreActor ! AddTwitterMessage(tweet, tweeter)
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
      (rawTweet: String) => {
        twitterProcessorActors ! rawTweet; true
      })
    if(!terms.isEmpty) {
      SearchTerms.addTerms(terms)
      actorSystem.actorOf(Props(new TwitterCollectorActor(twitterCollector))) ! StartTwitterCollector
    }
    else
      logger.warn("No search terms defined so not starting collector")
  }
}
