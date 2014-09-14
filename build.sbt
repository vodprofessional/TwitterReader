import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._
import org.vaadin.sbt.VaadinPlugin._
import org.vaadin.sbt.tasks.CompileWidgetsetsTask


packageArchetype.java_server

maintainer in Linux := "Mark Tolmacs <tolmi@lazycat.hu>"

packageSummary in Linux := "A proprietary social network collector and processor built for Vodprofessionals.com"

packageDescription := "Social Explorer is a proprietary software server written in Scala and Java with one goal. To collect and filter messages from social networks and process them into analytics which are displayed on the web."

bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""

bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""

batScriptExtraDefines += """set _JAVA_OPTS=%_JAVA_OPTS% -Dconfig.file=%SOCIALEXPLORER_HOME%\\conf\\application.conf -Dlogback.configurationFile=%SOCIALEXPLORER_HOME%\\conf\\logback.xml"""

name := "socialexplorer"

version := "2.0.1"

scalaVersion := "2.11.2"

resolvers ++= Seq(
  "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases",
  "Vaadin Addon"            at "http://maven.vaadin.com/vaadin-addons"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"          %% "akka-actor"                  % "2.3.4",
  "org.json4s"                 %% "json4s-jackson"              % "3.2.10",
  "com.fasterxml.jackson.core" %  "jackson-core"                % "2.3.1",
  "com.twitter"                %  "hbc-core"                    % "2.2.0",
  "com.typesafe"               %  "config"                      % "1.2.1",
  "com.typesafe.scala-logging" %% "scala-logging"               % "3.0.0",
  "ch.qos.logback"             %  "logback-classic"             % "1.1.2",
  "org.slf4j"                  %  "slf4j-api"                   % "1.7.7",
  "org.slf4j"                  %  "jul-to-slf4j"                % "1.7.7",
  "com.typesafe.slick"         %% "slick"                       % "2.1.0",
  "org.apache.httpcomponents"  %  "httpclient"                  % "4.3.5",
  "org.postgresql"             %  "postgresql"                  % "9.3-1102-jdbc4",
  "mysql"                      %  "mysql-connector-java"        % "5.1.31",
  "org.eclipse.jetty"          %  "jetty-servlet"               % "9.2.2.v20140723",
  "com.vaadin"                 %  "vaadin-server"               % "7.2.5",
  "com.vaadin"                 %  "vaadin-client-compiled"      % "7.2.5",
  "org.vaadin.addons"          %  "tokenfield"                  % "7.0.1",
  "com.vaadin"                 %  "vaadin-themes"               % "7.2.5"             % Compile,
  "com.vaadin"                 %  "vaadin-client-compiler"      % "7.2.5"             % Compile,
  "com.h2database"             %  "h2"                          % "1.3.148"           % Test
)

vaadinSettings

vaadinThemesDir := Seq((sourceDirectory in Compile).value / "resources" / "VAADIN" / "themes")

target in compileVaadinThemes := (sourceDirectory in Compile).value / "resources" / "VAADIN" / "themes"

target in compileVaadinWidgetsets := (sourceDirectory in Compile).value / "resources" / "VAADIN" / "widgetsets"
