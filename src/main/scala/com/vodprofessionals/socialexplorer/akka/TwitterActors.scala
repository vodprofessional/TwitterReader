package com.vodprofessionals.socialexplorer.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.RDBMSTwitterStoreMessages.AddTwitterMessage
import com.vodprofessionals.socialexplorer.akka.TwitterCollectorMessages.{StopTwitterCollector, RestartTwitterCollector, StartTwitterCollector}
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.persistence.TwitterStore
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor


object TwitterCollectorMessages {
  sealed trait CollectorMessage

  case object StartTwitterCollector extends CollectorMessage
  case object RestartTwitterCollector extends CollectorMessage
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
    case StartTwitterCollector =>
      twitterCollector.start

      context.become({
        case RestartTwitterCollector =>
          twitterCollector.stop
          twitterCollector.start

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
  case class AddTwitterMessage(tweet: Tweet, tweeter: Tweeter)
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
    case AddTwitterMessage(tweet: Tweet, tweeter: Tweeter) =>
      rdbmsTwitterStore.insertTweeter(tweeter)
      rdbmsTwitterStore.insertTweet(tweet)

  }

}
