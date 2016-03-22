import Dependencies._
import sbt.Keys._
import sbt._

object Build extends Build {
  lazy val processing = (project in file(".")).settings(
    organization in ThisBuild := "com.qiwi.processing",
    scalaVersion in ThisBuild := "2.11.8",
    scalacOptions in ThisBuild := Seq("-encoding", "utf8", "-feature", "-language:postfixOps", "-language:implicitConversions", "-deprecation"),
    updateOptions := updateOptions.value.withCachedResolution(true)
  ).settings(
    libraryDependencies ++= Seq(
      Akka.testkit,
      AkkaDDD.messaging, AkkaDDD.core, AkkaDDD.scheduling, AkkaDDD.test, AkkaDDD.eventStore,
      Akka.http,
      Akka.`http-xml`,
      Cats,
      AkkaHttpJson.`play-json`,
      NScalaTime,
      KryoSerialization,
      Akka.`cluster-sharding`,
      `logstash-appender`,
      `scala-logging`,
      KryoExtensions
    )
  )
}