import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import org.vaadin.sbt.VaadinPlugin._


packageArchetype.java_server

maintainer in Linux := "Mark Tolmacs <tolmi@lazycat.hu>"

packageSummary in Linux := "A proprietary social network collector and processor built for Vodprofessionals.com"

packageDescription := "Social Explorer is a proprietary software server written in Scala and Java with one goal. To collect and filter messages from social networks and process them into analytics which are displayed on the web."

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""

batScriptExtraDefines += """set _JAVA_OPTS=%_JAVA_OPTS% -Dconfig.file=%SOCIALEXPLORER_HOME%\\conf\\application.conf"""

name := "socialexplorer"

version := "0.0.2"

scalaVersion := "2.11.2"

resolvers += "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases"

resolvers += "Scaladin Snapshots" at "http://henrikerola.github.io/repository/snapshots/"

libraryDependencies ++= Seq(
  "com.vaadin"                 %  "vaadin-server"           % "7.2.5",
  "com.vaadin"                 %  "vaadin-themes"           % "7.2.5", //% "container",
  "com.vaadin"                 %  "vaadin-client-compiled"  % "7.2.5",
  "com.vaadin"                 %  "vaadin-client-compiler"  % "7.2.5", //% "container",
  "com.vaadin"                 %  "vaadin-push"             % "7.2.5", //% "container",
  "com.typesafe.akka"          %% "akka-actor"              % "2.3.4",
  "org.json4s"                 %% "json4s-jackson"          % "3.2.10",
  "com.twitter"                %  "hbc-core"                % "2.2.0",
  "com.typesafe"               %  "config"                  % "1.2.1",
  "com.typesafe.scala-logging" %% "scala-logging"           % "3.0.0",
  "org.slf4j"                  %  "slf4j-jdk14"             % "1.7.7",
  "com.typesafe.slick"         %% "slick"                   % "2.1.0-RC2",
  "org.postgresql"             %  "postgresql"              % "9.3-1102-jdbc4",
  "mysql"                      %  "mysql-connector-java"    % "5.1.31",
  "com.h2database"             %  "h2"                      % "1.3.148" % Test,
  "org.eclipse.jetty"          %  "jetty-server"            % "9.2.2.v20140723",
  "org.eclipse.jetty.websocket"%  "javax-websocket-server-impl" % "9.2.2.v20140723",
  "javax.websocket"            %  "javax.websocket-api"     % "1.0",
  "javax.servlet"              %  "javax.servlet-api"       % "3.1.0" % "provided",
  "org.eclipse.jetty"          %  "jetty-plus"              % "9.2.2.v20140723" % "container",
  "org.eclipse.jetty"          %  "jetty-server"            % "9.2.2.v20140723" % "container",
  "org.eclipse.jetty"          %  "jetty-servlet"           % "9.2.2.v20140723" % "container",
  "org.eclipse.jetty"          %  "jetty-webapp"            % "9.2.2.v20140723" % "container",
  "javax.servlet.jsp"          %  "jsp-api"                 % "2.2"             % "container"
)

target in compileVaadinWidgetsets := (sourceDirectory in Compile).value / "webapp" / "VAADIN" / "widgetsets"

vaadinWebSettings

//webSettings
