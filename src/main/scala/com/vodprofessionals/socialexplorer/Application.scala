package com.vodprofessionals.socialexplorer

import com.typesafe.scalalogging.LazyLogging
import hu.lazycat.scala.config.Configurable
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import javax.websocket.server.ServerContainer;
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}

/**
 *
 */
object Application extends App with LazyLogging with Configurable {

  try {
    val server:Server = new Server(8080);

    val context:ServletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    val webDir = this.getClass().getClassLoader().getResource("webapp/").toExternalForm();
    context.setResourceBase(webDir);
    server.setHandler(context);

    val container:ServerContainer = WebSocketServerContainerInitializer.configureContext( context );

    val servletHolder = new ServletHolder(new com.vaadin.server.VaadinServlet());
    servletHolder.setInitParameter("pushmode", "automatic");
    servletHolder.setInitParameter("productionMode", "false");
    servletHolder.setInitParameter("UI", "com.vodprofessionals.socialexplorer.web.DashboardUI");
    servletHolder.setInitParameter("widgetset", "com.vodprofessionals.socialexplorer.web.DashboardUI");
    servletHolder.setAsyncSupported(true);
    servletHolder.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.JSR356AsyncSupport");

    context.addServlet(servletHolder, "/*");

    server.start();
    server.join();

    val twitterCollector = new TwitterCollector
    //twitterCollector.start
  } catch {
    case ex:Exception => logger.error(ex.getMessage, ex)
  }

}
