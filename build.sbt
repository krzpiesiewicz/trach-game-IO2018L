lazy val root = (project in file("."))
  .aggregate(game)
  .settings(commonSettings: _*)
  
lazy val game = (project in file("game"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.google.inject" % "guice" % "4.2.2"
    )
  )
  
lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.example",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  scalacOptions ++= Seq("-feature")
)
