package com.vodprofessionals.socialexplorer.web

import com.vodprofessionals.socialexplorer.config.Configurable
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.security.{ConstraintSecurityHandler, ConstraintMapping, HashLoginService}
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import org.eclipse.jetty.util.security.{Constraint, Credential}

/**
 *
 */
class WebServer extends LazyLogging with Configurable {
  val server         = new Server();
  val context        = new ServletContextHandler(ServletContextHandler.SESSIONS);


  try {
    val servletHolder  = new ServletHolder(new com.vaadin.server.VaadinServlet());
    servletHolder.setAsyncSupported(true);
    for ((key, value) <- Map(
      "productionMode"                  -> getStringConfigOrElse("web.vaadin.productionMode", "true"),
//      "UI"                              -> "com.vodprofessionals.socialexplorer.vaadin.DashboardUI",
      "widgetset"                       -> "com.vodprofessionals.socialexplorer.vaadin.DashboardWidgetSet",
      "UIProvider"                      -> "com.vodprofessionals.socialexplorer.vaadin.DashboardUIProvider"))
    yield servletHolder.setInitParameter(key, value)

    context.setContextPath("/")
    context.addServlet(servletHolder, "/*")

    val securityUser = CONFIG.getString("web.security.user")
    val securityPassword = CONFIG.getString("web.security.password")

    val loginService: HashLoginService = new HashLoginService()
    loginService.putUser(securityUser, Credential.getCredential(securityPassword), Array("user"))
    loginService.setName("Provide a valid user/password")

    val constraint = new Constraint()
    constraint.setName(Constraint.__BASIC_AUTH)
    constraint.setRoles(Array("user"))
    constraint.setAuthenticate(true)

    val cm = new ConstraintMapping()
    cm.setConstraint(constraint)
    cm.setPathSpec("/*")

    val csh = new ConstraintSecurityHandler()
    csh.setAuthenticator(new BasicAuthenticator())
    csh.setRealmName("Social Explorer")
    csh.addConstraintMapping(cm)
    csh.setLoginService(loginService)

    context.setSecurityHandler(csh)
  } catch {
    case ex: Throwable => logger.error("ERROR STARTING WEB SERVER: " + ex.getMessage, ex)
  }



  /**
   *
   */
  def start(port: Int) = {
    val httpConnector = new ServerConnector(server)
    httpConnector.setPort(port)
    server.addConnector(httpConnector)
    server.setHandler(context)
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
