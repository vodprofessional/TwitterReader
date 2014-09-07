package com.vodprofessionals.socialexplorer.config

import com.typesafe.config.ConfigFactory

/**
 * Mix this trait in if you want convenient access to Typesafe Scala Config configuration
 */
trait Configurable {
  lazy val CONFIG = ConfigFactory.load()

  def getStringConfigOrElse(name: String, default: String) = try {
      CONFIG.getString(name)
    } catch {
      case _: Throwable => default
    }
}
