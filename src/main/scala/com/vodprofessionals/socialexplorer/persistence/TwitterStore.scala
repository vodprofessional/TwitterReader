package com.vodprofessionals.socialexplorer.persistence

import com.vodprofessionals.socialexplorer.domain.Tweet

/**
 *
 */
abstract class TwitterStore {
  /**
   *
   * @param tweet
   */
  def insert(tweet: Tweet)
}
