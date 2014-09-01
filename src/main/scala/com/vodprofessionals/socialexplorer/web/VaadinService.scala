package com.vodprofessionals.socialexplorer.web

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import scala.concurrent.{Future, Promise}


case class StartVaadinService(port: Int)
case class StopVaadinService()

/**
 * A root service to start the web interface.
 *
 */
class VaadinService(
            val webServer: WebServer
                   ) extends Actor with LazyLogging {

  /**
   *
   * @return
   */
  def receive = {

    case StartVaadinService(port) =>
      webServer.start(port)


    case StopVaadinService =>
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
