package com.vodprofessionals.socialexplorer

import com.typesafe.scalalogging.slf4j.Logging
import hu.lazycat.scala.config.AppConfig

/**
 *
 */
object Application extends App with Logging {
  val worker = new Worker
  worker.start

  worker.stop
}
