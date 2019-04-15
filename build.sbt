lazy val root = (project in file("."))
  .aggregate(game, server)
  .settings(commonSettings: _*)
  
lazy val game = (project in file("game"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.google.inject" % "guice" % "4.2.2",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      playJson,
    )
  )
  
lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
        guice,
        specs2 % Test,
        macwireMacros,
        macwireUtil,
        macwireProxy,
        playJson,
    ),
    routesGenerator := play.routes.compiler.InjectedRoutesGenerator,
    PlayKeys.devSettings ++= Seq(
      "play.server.http.port" -> "disabled",
      "play.server.http.idleTimeout" -> "180s",
      "play.client.http.idleTimeout" -> "180s",
      "play.server.https.port" -> "9001",
      "play.server.https.idleTimeout" -> "180s",
      "play.client.https.idleTimeout" -> "180s",
      "engineProvider" -> "play.core.server.ssl.DefaultSSLEngineProvider"
    )
  )
  .enablePlugins(PlayScala)
  .dependsOn(game)
  
lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.example",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  scalacOptions ++= Seq("-feature")
)

lazy val akkaVersion = "2.5.19"

lazy val playJson = "com.typesafe.play" %% "play-json" % "2.7.0-RC2"

val macwireVersion = "2.3.1"
val macwireMacros = "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided
val macwireUtil = "com.softwaremill.macwire" %% "util" % macwireVersion
val macwireProxy = "com.softwaremill.macwire" %% "proxy" % macwireVersion
