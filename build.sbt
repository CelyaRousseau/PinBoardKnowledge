name := "PinBoardKnowledge"

version := "1.0"

lazy val `pinboardknowledge` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "net.debasishg" %% "redisclient" % "3.0" sr
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  