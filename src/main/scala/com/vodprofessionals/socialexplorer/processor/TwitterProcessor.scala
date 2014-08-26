package com.vodprofessionals.socialexplorer.processor

import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.domain.{Tweet, RawTweet}
import com.vodprofessionals.socialexplorer.model.SearchTerms


/**
 *
 */
class TwitterProcessor(
                        val storage: Tweet => Unit
                       ) extends LazyLogging {


  /*
   *
   */
  def process(rawTweet: RawTweet) = rawTweet match {
    case RawTweet(jsonMessage: String) =>
      val tweet = Tweet(jsonMessage, SearchTerms.matchTerms(jsonMessage))

      // Any processing of the Tweet data comes here...
      if (tweet.term.length > 0) {
        storage(tweet)
      }
  }
}
