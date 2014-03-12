package hu.lazycat.scala.config

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import com.typesafe.scalalogging.slf4j.Logging

object AppConfig extends Logging {
  val env = scala.util.Properties.envOrElse("MODE", "prod")
  val config:Config = ConfigFactory.parseFile(new File("conf/application." + env + ".conf"))
}
