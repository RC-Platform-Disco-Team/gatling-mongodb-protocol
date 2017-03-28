import io.gatling.sbt.GatlingPlugin

enablePlugins(GatlingPlugin)

name := "gatling-mongo"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

libraryDependencies ++= Seq(
  "io.gatling"            % "gatling-test-framework" % "2.2.0" % "compile",
  "com.typesafe.play"    %% "play-json" % "2.5.10" % "compile",
  "org.reactivemongo"    %% "reactivemongo" % "0.12.1" % "compile",
  "org.reactivemongo"    %% "reactivemongo-play-json" % "0.12.1" % "compile",
  "org.reactivemongo"    %% "reactivemongo-iteratees" % "0.12.1" % "compile",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.0" % "test"
)
    