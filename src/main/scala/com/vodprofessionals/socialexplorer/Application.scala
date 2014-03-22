package com.vodprofessionals.socialexplorer

import com.typesafe.scalalogging.slf4j.Logging
import hu.lazycat.scala.config.AppConfig
import com.vodprofessionals.socialexplorer.collector.TwitterCollector

/**
 *
 */
object Application extends App with Logging {
  try {
    val twitterCollector = new TwitterCollector
    twitterCollector.start
  } catch {
    case ex:Exception => logger.error(ex.getMessage, ex)
  }


}
