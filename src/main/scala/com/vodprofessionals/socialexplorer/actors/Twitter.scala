package com.vodprofessionals.socialexplorer.actors

import akka.actor.Actor
import com.vodprofessionals.socialexplorer.collector.CollectorMessages.{RestartTwitterCollector, StopTwitterCollector, StartTwitterCollector}
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import com.vodprofessionals.socialexplorer.domain.RawTweet
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor


/**
 *
 */
class TwitterCollectorActor (
          val twitterCollector: TwitterCollector
                            ) extends Actor {


  /**
   *
   * @return
   */
  def receive = {
    case StartTwitterCollector(terms: List[String]) =>
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
    case rawTweet: RawTweet =>
      twitterProcessor.process(rawTweet)
  }
}
