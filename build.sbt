name := """account-webapp"""
organization := "com.herasimov"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.10.0" % Test

// Database access
libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "play-slick" % "3.0.2",
  "com.h2database" % "h2" % "1.4.196"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.herasimov.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.herasimov.binders._"
