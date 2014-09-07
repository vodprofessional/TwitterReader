package com.vodprofessionals.socialexplorer.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.akka.WebServerActor.{StopWebServer, StartWebServer}
import com.vodprofessionals.socialexplorer.web.WebServer


object WebServerActor {
  case class StartWebServer(port: Int)
  case class StopWebServer()
}


/**
 * A root service to start the web interface.
 *
 */
class WebServerActor(
            val webServer: WebServer
                   ) extends Actor with LazyLogging {

  /**
   *
   * @return
   */
  def receive = {

    case StartWebServer(port) =>
      webServer.start(port)


    case StopWebServer =>
      if (webServer.isStarted)
        try {
          webServer.stop
        } catch {
          case _: Throwable => {}
        }
  }

  /**
   *
   */
  override def postStop =
    try {
      webServer.stop
    } catch {
      case _: Throwable => {}
    }
}
