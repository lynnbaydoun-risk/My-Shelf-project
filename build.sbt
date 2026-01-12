name := """myShelf"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.18"
resolvers += Resolver.mavenCentral

//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "org.postgresql" % "postgresql" % "42.7.3"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
