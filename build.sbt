lazy val root = (project in file("."))
  .aggregate(jvmapi, game, bot, server)
  .settings(commonSettings: _*)
  
lazy val jvmapi = (project in file("jvmapi"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      playJson,
    )
  )
  
lazy val game = (project in file("game"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.google.inject" % "guice" % "4.2.2",
      akkaActor,
      akkaTestKit,
      playJson,
    )
  ).dependsOn(jvmapi)
  
lazy val bot = (project in file("bot"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaTestKit,
      "junit" % "junit" % "4.12" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep")
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
  .dependsOn(jvmapi, game, bot)
  
lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.example",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  scalacOptions ++= Seq("-feature"),
)

lazy val akkaVersion = "2.5.19"
lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion

lazy val playJson = "com.typesafe.play" %% "play-json" % "2.7.3"

val macwireVersion = "2.3.1"
val macwireMacros = "com.softwaremill.macwire" %% "macros" % macwireVersion % Provided
val macwireUtil = "com.softwaremill.macwire" %% "util" % macwireVersion
val macwireProxy = "com.softwaremill.macwire" %% "proxy" % macwireVersion

resolvers in ThisBuild += "Artima Maven Repository" at "http://repo.artima.com/releases"
