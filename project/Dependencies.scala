
import sbt._

object Dependencies extends DependencyHelpers {

  object Version {
    val akka =              "2.4.2"
    val `akka-ddd` =        "1.0.10-Q"
    val shapeless =         "2.2.5"
    val nScalaTime =        "2.6.0"
    val kryoSerialization = "0.4.0"
    val circe =             "0.3.0"
    val `slick-pg` =        "0.10.2"
    val `akka-http-json` =  "1.5.0"
    val cats =              "0.4.1"
    val `scala-bcrypt` =    "2.5"
    val `logstash-appender` = "4.6"
    val `scala-logging` =  "3.1.0"
    val json4s =           "3.3.0"
  }


  val Shapeless = "com.chuusai" %% "shapeless" % Version.shapeless

  val Cats = "org.typelevel" %% "cats" % Version.cats

  val NScalaTime = "com.github.nscala-time" %% "nscala-time" % Version.nScalaTime

  val `scala-bcrypt` = "com.github.t3hnar" %% "scala-bcrypt" % Version.`scala-bcrypt`

  val `logstash-appender` = "net.logstash.logback" % "logstash-logback-encoder" % Version.`logstash-appender`

  object Akka extends Multimodule("com.typesafe.akka", "akka", Version.akka) {
    val actor =            apply("actor")
    val cluster =          Akka("cluster")
    val `cluster-sharding` = Akka("cluster-sharding")
    val httpTestKit =      apply("http-testkit") % "test"
    val persistence =      apply("persistence")
    val slf4j =            apply("slf4j")
    val testkit =          apply("testkit") % "test"
    val multiNodeTestkit = apply("multi-node-testkit") % "test"
    val http = apply("http-experimental")
    val `http-xml` = apply("http-xml-experimental")
  }

  val `scala-logging` = "com.typesafe.scala-logging" %% "scala-logging" % Version.`scala-logging`

  object AkkaHttpJson extends Multimodule("de.heikoseeberger", "akka-http", Version.`akka-http-json`) {
    val `play-json` = AkkaHttpJson("play-json")
    val circe = AkkaHttpJson("circe")
  }

  val KryoSerialization = "com.github.romix.akka" %% "akka-kryo-serialization" % Version.kryoSerialization

  val KryoExtensions = "de.javakaffee" % "kryo-serializers" % "0.37"

  object AkkaDDD {
    val messaging = apply("messaging")
    val core = apply("core")
    val writeFront = apply("write-front")
    val httpSupport = Seq(
      "pl.newicom.dddd" %% s"http-support" % Version.`akka-ddd`,
      Akka.httpTestKit
    )
    val viewUpdateSql = "pl.newicom.dddd" %% "view-update-sql" % Version.`akka-ddd`
    val eventStore = "pl.newicom.dddd" %% "eventstore-akka-persistence" % Version.`akka-ddd`
    val scheduling = apply("scheduling")
    val test = apply("test") % "test"
    private def apply(moduleName: String) = "pl.newicom.dddd" %% s"akka-ddd-$moduleName" % Version.`akka-ddd`
  }

  object Json {
    val `4s`  = Seq(Json4s.native, Json4s.ext)
    val circe = Seq(Circe.generic, Circe.core, Circe.parse)
  }

  object Circe extends Multimodule("io.circe", "circe", Version.circe) {
    val generic = Circe("generic")
    val core = Circe("core")
    val parse = Circe("parse")
    val jawn = Circe("jawn")
    val refined = Circe("refined")
  }

  object Json4s extends Multimodule("org.json4s", "json4s", Version.json4s) {
    val native = apply("native")
    val ext = apply("ext")
  }

  object SqlDb {
    val `slick-for-pg` = "com.github.tminglei" %% "slick-pg" % Version.`slick-pg` exclude("org.slf4j", "slf4j-simple")
    val `slick-pg_joda-time` = "com.github.tminglei" %% "slick-pg_joda-time" % Version.`slick-pg` exclude("org.slf4j", "slf4j-simple")
    val testDriver = "com.h2database" % "h2" % "1.4.189" % "test"

    def apply() = Seq(`slick-for-pg`, `slick-pg_joda-time`, testDriver)
  }
}

trait DependencyHelpers {
  abstract class Multimodule(organization: String, name: String, version: String) {
    protected def apply(module: String): ModuleID = organization %% s"$name-$module" % version
  }
}