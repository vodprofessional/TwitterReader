package com.vodprofessionals.socialexplorer

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import com.vodprofessionals.socialexplorer.processor.TwitterProcessor
import hu.lazycat.scala.config.Configurable
import com.vodprofessionals.socialexplorer.collector.TwitterCollector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import javax.websocket.server.ServerContainer;
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}

/**
 *
 */
object Application extends LazyLogging {
  var workers: Map[String, Thread] = Map.empty

  def main(args: Array[String]) = {
    if (args.length > 0) {
      // Looks like we are a web node
      try {
        val webServerThread = new Thread(new Runnable() {
          def run(): Unit = {
            startWebServer(Integer.valueOf(args(0)))
          }
        })
        webServerThread.setPriority(100)
        workers += "web" -> webServerThread
      } catch {
        case ex: Exception => logger.error(ex.getMessage, ex)
      }
    }

    try {
      // Attempt to start a Twitter message processor
      val twitterProcessorThread = new Thread(new Runnable() {
        def run(): Unit = {
          val twitterProcessor = new TwitterProcessor
          twitterProcessor.start
        }
      })
      twitterProcessorThread.setPriority(50)
      workers += "twitter-processor" -> twitterProcessorThread
    } catch {
      case ex:Exception => logger.error(ex.getMessage, ex)
    }

    try {
      // Attempt to start Twitter Firehose reader worker
      val twitterFirehoseThread = new Thread(new Runnable() {
        def run(): Unit = {
          val twitterCollector = new TwitterCollector
          twitterCollector.start
        }
      })
      twitterFirehoseThread.setPriority(0)
      workers += "twitter-reader" -> twitterFirehoseThread
    } catch {
      case ex: Exception => logger.error(ex.getMessage, ex)
    }
  }



  /**
   *
   * @param port
   */
  def startWebServer(port: Int) = {
    val context: ServletContextHandler  = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.setResourceBase(
      this.getClass().getClassLoader().getResource("webapp/").toExternalForm()
    );

    val container: ServerContainer = WebSocketServerContainerInitializer.configureContext( context );
    val servletHolder              = new ServletHolder(new com.vaadin.server.VaadinServlet());
    servletHolder.setInitParameter("pushmode", "automatic");
    servletHolder.setInitParameter("productionMode", "false");
    servletHolder.setInitParameter("UI",        "com.vodprofessionals.socialexplorer.web.DashboardUI");
    servletHolder.setInitParameter("widgetset", "com.vodprofessionals.socialexplorer.web.DashboardUI");
    servletHolder.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.JSR356AsyncSupport");
    servletHolder.setAsyncSupported(true);
    context.addServlet(servletHolder, "/*");

    val server: Server = new Server(port);
    server.setHandler(context);
    server.start();
    server.join();
  }
}
