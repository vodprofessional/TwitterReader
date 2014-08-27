package com.vodprofessionals.socialexplorer.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.RDBMSTwitterStoreMessages.AddTwitterMessage
import com.vodprofessionals.socialexplorer.akka.TwitterCollectorMessages.{StopTwitterCollector, RestartTwitterCollector, StartTwitterCollector}
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import com.vodprofessionals.socialexplorer.domain.Tweet
import com.vodprofessionals.socialexplorer.persistence.TwitterStore
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor


object TwitterCollectorMessages {
  sealed trait CollectorMessage

  case class StartTwitterCollector(terms: List[String]) extends CollectorMessage
  case class RestartTwitterCollector(terms: List[String]) extends CollectorMessage
  case object StopTwitterCollector extends CollectorMessage
}

/**
 *
 */
class TwitterCollectorActor ( val twitterCollector: TwitterCollector ) extends Actor with LazyLogging {

  /**
   *
   * @return
   */
  def receive = {
    case StartTwitterCollector(terms) =>
      logger.error("TwitterCollectorActor START")
      twitterCollector.start(terms)

      context.become({
        case RestartTwitterCollector(terms: List[String]) =>
          twitterCollector.stop
          twitterCollector.start(terms)

        case StopTwitterCollector =>
          twitterCollector.stop
          context.unbecome()

      }, discardOld = false)
  }

  /**
   *
   */
  override def postStop =
    twitterCollector.stop
}

/**
 *
 */
class TwitterProcessorActor(
        val twitterProcessor: TwitterProcessor
                           ) extends Actor {


  /**
   * @return
   */
  def receive = {
    case rawTweet: String =>
      twitterProcessor.process(rawTweet)
  }
}


object RDBMSTwitterStoreMessages {
  case class AddTwitterMessage(tweet: Tweet)
}

/**
 *
 */
class RDBMSTwitterStoreActor (
          val rdbmsTwitterStore: TwitterStore
                           ) extends Actor {

  /**
   *
   * @return
   */
  def receive = {
    case AddTwitterMessage(tweet: Tweet) =>
      rdbmsTwitterStore.insert(tweet)

  }

}
