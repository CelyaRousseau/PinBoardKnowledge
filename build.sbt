name := "PinBoardKnowledge"

version := "1.0"

lazy val `pinboardknowledge` = (project in file(".")).enablePlugins(PlayScala)

scalacOptions ++= Seq(
  "-Xlint:-missing-interpolator,_",
  "-deprecation",
  "-Xfatal-warnings",
  "-feature"
)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "net.debasishg" %% "redisclient" % "3.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

routesGenerator := InjectedRoutesGenerator
