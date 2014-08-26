package com.vodprofessionals.socialexplorer.web

import akka.actor.Actor
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import scala.concurrent.{Future, Promise}


case class StartVaadinService()
case class StopVaadinService()

/**
 * A root service to start the web interface.
 *
 */
class VaadinService extends Actor {
  val server: Server = new Server();


  override def preStart = {

  }

  /**
   *
   * @return
   */
  def receive = {

    case StartVaadinService => {
      val context        = new ServletContextHandler(ServletContextHandler.SESSIONS);
      val servletHolder  = new ServletHolder(new com.vaadin.server.VaadinServlet());

      servletHolder.setAsyncSupported(true);
      for ((key, value) <- Map(
        "pushmode"                        -> "automatic",
        "productionMode"                  -> "false",
        "UI"                              -> "com.vodprofessionals.socialexplorer.web.DashboardUI",
        "widgetset"                       -> "com.vodprofessionals.socialexplorer.web.DashboardUI",
        "org.atmosphere.cpr.asyncSupport" -> "org.atmosphere.container.JSR356AsyncSupport"))
      yield servletHolder.setInitParameter(key, value)
      context.setContextPath("/")
      context.setResourceBase(this.getClass().getClassLoader().getResource("webapp/").toExternalForm())
      context.addServlet(servletHolder, "/*")
      WebSocketServerContainerInitializer.configureContext( context )

      val httpConnector = new ServerConnector(server)
      httpConnector.setPort(8080)
      server.addConnector(httpConnector)
      server.setHandler(context)
      server.start()
    }

    case StopVaadinService =>
      if (server.isStarted)
        try {
          server.stop()
        } catch {
          case _: Throwable => {}
        }
  }

  /**
   *
   */
  override def postStop =
    try {
      server.stop()
    } catch {
      case _: Throwable => {}
    }
}
