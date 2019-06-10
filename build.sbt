lazy val root = (project in file("."))
  .aggregate(jvmapi, game, bot, server)
  .settings(commonSettings: _*)
  
lazy val jvmapi = (project in file("jvmapi"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      playJson,
      akkaActor
    )
  )
  
lazy val game = (project in file("game"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      logbackClassic,
      guice,
      akkaActor,
      akkaTestKit,
      akkaSlf4j,
      playJson
    )
  ).dependsOn(jvmapi)
  
lazy val bot = (project in file("bot"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaTestKit,
      akkaSlf4j,
      playLogback,
      logbackClassic
    )
  ).dependsOn(jvmapi)
  
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
        akkaActor,
        akkaSlf4j,
        logbackClassic
    ),
    routesGenerator := play.routes.compiler.InjectedRoutesGenerator,
    PlayKeys.devSettings ++= Seq(
      "play.server.http.port" -> "9000",
      "play.server.http.idleTimeout" -> "1000s",
      "play.client.http.idleTimeout" -> "1000s",
      "play.server.https.port" -> "9001",
      "play.server.https.idleTimeout" -> "1000s",
      "play.client.https.idleTimeout" -> "1000s",
      "akka.receive" -> "on"
//       "play.server.https.engineProvider" -> "play.core.server.ssl.DefaultSSLEngineProvider"
//       "play.server.https.engineProvider" -> "main.CustomSSLEngineProvider"
    )
  )
  .enablePlugins(PlayScala)
  .dependsOn(jvmapi, game, bot)
  
lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.example",
  libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  ),
  scalacOptions ++= Seq("-feature"),
)

lazy val akkaVersion = "2.5.19"
lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
lazy val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

lazy val playVersion = "2.7.2"
lazy val playJson = "com.typesafe.play" %% "play-json" % playVersion
lazy val playLogback = "com.typesafe.play" %% "play-logback" % playVersion

val macwireVersion = "2.3.1"
val macwireMacros = "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided
val macwireUtil = "com.softwaremill.macwire" %% "util" % macwireVersion
val macwireProxy = "com.softwaremill.macwire" %% "proxy" % macwireVersion

resolvers in ThisBuild += "Artima Maven Repository" at "http://repo.artima.com/releases"
