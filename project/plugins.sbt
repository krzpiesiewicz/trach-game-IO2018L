resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

lazy val playVersion = "2.7.2"

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.7")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")
