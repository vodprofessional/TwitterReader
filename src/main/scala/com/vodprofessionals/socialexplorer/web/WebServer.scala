package com.vodprofessionals.socialexplorer.web

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer

/**
 *
 */
class WebServer extends LazyLogging {
  val server         = new Server();
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


  /**
   *
   */
  def start(port: Int) = {
    val httpConnector = new ServerConnector(server)
    httpConnector.setPort(port)
    server.addConnector(httpConnector)
    server.setHandler(context)
    WebSocketServerContainerInitializer.configureContext( context )
    server.start
    logger.info("Web server started")
  }

  /**
   *
   */
  def stop = {
    server.stop
    logger.info("Web server stopped")
  }

  /**
   *
   * @return
   */
  def isStarted = server.isStarted
}
