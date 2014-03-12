import com.typesafe.sbt.SbtNativePackager.packageArchetype

packageArchetype.java_application

name := "socialexplorer"

version := "0.0.1"

scalaVersion := "2.10.2"

resolvers += "twitter-repo" at "http://maven.twttr.com"

libraryDependencies ++= Seq(
  "com.twitter"         %  "hbc-core"             % "1.4.2",
  "com.typesafe"        % "scalalogging-slf4j_2.10" % "1.1.0",
  "com.typesafe"        %  "config"               % "1.2.0",
  "com.typesafe.slick"  %% "slick"                % "2.0.0",
  "org.postgresql"      % "postgresql"            % "9.3-1100-jdbc4",
  "com.h2database"      % "h2"                    % "1.3.175",
  "mysql"               % "mysql-connector-java"  % "5.1.29",
  "ch.qos.logback"      % "logback-classic"       % "1.1.1"
)


