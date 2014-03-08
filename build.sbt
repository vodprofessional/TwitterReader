import com.typesafe.sbt.SbtNativePackager.packageArchetype

packageArchetype.java_application

name := "socialscanner"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "twitter-repo" at "http://maven.twttr.com"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "[1.6.6,1.7.0[",
  "com.twitter" % "hbc-core" % "[1.4.2,1.5.0[",
  "com.typesafe.slick" %% "slick" % "[2.0.0,2.1.0["
)


