package com.vodprofessionals.socialexplorer

import com.typesafe.scalalogging.slf4j.Logging
import hu.lazycat.scala.config.AppConfig

/**
 *
 */
object Application extends App with Logging {
  try {
    val worker = new Worker
    worker.start
  } catch {
    case ex:Exception => logger.error(ex.getMessage, ex)
  }


}
