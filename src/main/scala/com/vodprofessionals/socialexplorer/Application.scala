package com.vodprofessionals.socialexplorer

import java.util.logging.{Level, Logger, LogManager}

import _root_.akka.actor.{Props, ActorSystem}
import _root_.akka.routing.RoundRobinGroup
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka._
import com.vodprofessionals.socialexplorer.config.Configurable
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import com.vodprofessionals.socialexplorer.persistence.{ContextAwareRDBMSDriver, ContextAwareRDBMSProfile, SlickComponents, SlickTwitterStore}
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import com.vodprofessionals.socialexplorer.web.WebServer
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import org.slf4j.bridge.SLF4JBridgeHandler

import _root_.scala.slick.driver.JdbcProfile


/**
 *
 */
object Application extends App with LazyLogging with Configurable {
  val actorSystem    = ActorSystem("socialStreamNodeSystem")
  val processorCores = Runtime.getRuntime().availableProcessors()
  val nrOfWorkers    = if (processorCores > 1) (processorCores - 1) * 2 else 2


  try {
    // Set up slf4j bridge to catch j.u.l messages as well
    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()
    Logger.getLogger("global").setLevel(Level.FINEST);

    // Start the web service if needed
    val port = Integer.parseInt(args.head)

    if (port > 0)
      actorSystem.actorOf(Props(new WebServerActor(new WebServer))) ! WebServerActor.StartWebServer(port)

    // Attempt to create the Slick RDBMS twitter store
    val slickStoreActor = actorSystem.actorOf(Props(new RDBMSTwitterStoreActor(new SlickTwitterStore())))

    // Attempt to boot up the Twitter processor actors
    val actorPaths = for (i <- 1 to nrOfWorkers)
      yield actorSystem.actorOf(Props(new TwitterProcessorActor(new TwitterProcessor(
          (tweet: Tweet, tweeter: Tweeter) => slickStoreActor ! RDBMSTwitterStoreActor.AddTwitterMessage(tweet, tweeter)
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
      }
    )

    if(!terms.isEmpty) {
      SearchTerms.addTerms(terms)
      //actorSystem.actorOf(Props(new TwitterCollectorActor(twitterCollector))) ! TwitterCollectorActor.StartTwitterCollector
    }
    else
      logger.warn("No search terms defined so not starting collector")
  } catch {
    case ex: Throwable => logger.error(ex.getMessage, ex)
  }

}
