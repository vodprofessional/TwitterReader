package com.vodprofessionals.socialexplorer

import com.typesafe.scalalogging.LazyLogging
import hu.lazycat.scala.config.Configurable
import com.vodprofessionals.socialexplorer.collector.TwitterCollector

/**
 *
 */
object Application extends App with LazyLogging with Configurable {

  try {
    val twitterCollector = new TwitterCollector
    twitterCollector.start
  } catch {
    case ex:Exception => logger.error(ex.getMessage, ex)
  }

}
