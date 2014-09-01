package com.vodprofessionals.socialexplorer.persistence

import com.vodprofessionals.socialexplorer.domain.{Tweeter, Tweet}

/**
 *
 */
abstract class TwitterStore {
  /**
   *
   * @param tweet
   */
  def insertTweet(tweet: Tweet)

  /**
   *
   * @param tweeter
   */
  def insertTweeter(tweeter: Tweeter)
}
