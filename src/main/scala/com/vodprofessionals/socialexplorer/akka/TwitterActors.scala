package com.vodprofessionals.socialexplorer.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.RDBMSTwitterStoreActor.AddTwitterMessage
import com.vodprofessionals.socialexplorer.akka.TwitterCollectorActor.{RestartIfTermsDirty, StopTwitterCollector, RestartTwitterCollector, StartTwitterCollector}
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms
import com.vodprofessionals.socialexplorer.persistence.TwitterStore
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import scala.concurrent.duration._


object TwitterCollectorActor {
  sealed trait CollectorMessage

  case object StartTwitterCollector extends CollectorMessage
  case object RestartTwitterCollector extends CollectorMessage
  case object StopTwitterCollector extends CollectorMessage
  case object RestartIfTermsDirty extends CollectorMessage
}

/**
 *
 */
class TwitterCollectorActor ( val twitterCollector: TwitterCollector ) extends Actor with LazyLogging {
  import context.dispatcher

  val periodicRefresher = context.system.scheduler.schedule(5 minutes, 5 minutes, self, RestartIfTermsDirty)


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

        case RestartIfTermsDirty =>
          if (SearchTerms.isDirty) {
            logger.info("Restarted twitter collector due to dirty terms list")
            SearchTerms.commitDirty
            self ! RestartTwitterCollector
          }

      }, discardOld = false)

    case RestartIfTermsDirty =>
      SearchTerms.commitDirty
      self ! StartTwitterCollector
  }

  /**
   *
   */
  override def postStop = {
    periodicRefresher.cancel()
    twitterCollector.stop
  }

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


object RDBMSTwitterStoreActor {
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
