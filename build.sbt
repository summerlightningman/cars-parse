name := "cars-parse"

version := "0.1"

scalaVersion := "2.13.1"

fork := false

javacOptions ++= Seq("-Dfile.encoding", "UTF-8")

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "net.liftweb" %% "lift-json" % "3.4.1"
)

