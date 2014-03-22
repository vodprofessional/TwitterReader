import com.typesafe.sbt.SbtNativePackager.packageArchetype

packageArchetype.java_application

name := "socialexplorer"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.json4s"                %% "json4s-jackson"          % "3.2.7",
  "org.apache.httpcomponents" %  "httpclient"              % "4.2.5",
  "com.google.guava"          %  "guava"                   % "13.0.1",
  "org.slf4j"                 %  "slf4j-api"               % "1.6.6",
  "com.twitter"               %  "joauth"                  % "6.0.2",
  "com.google.code.findbugs"  %  "jsr305"                  % "1.3.9",
  "com.typesafe"              %  "scalalogging-slf4j_2.10" % "1.1.0",
  "com.typesafe"              %  "config"                  % "1.2.0",
  "com.typesafe.slick"        %% "slick"                   % "2.0.0",
  "org.postgresql"            %  "postgresql"              % "9.3-1100-jdbc4",
  "com.h2database"            %  "h2"                      % "1.3.175",
  "mysql"                     %  "mysql-connector-java"    % "5.1.29",
  "ch.qos.logback"            %  "logback-classic"         % "1.1.1"
)
