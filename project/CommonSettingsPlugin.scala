
import com.typesafe.sbt.packager.docker.DockerKeys
import sbt.Keys._
import sbt._

object CommonSettingsPlugin extends AutoPlugin with DockerKeys {
  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
      "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven",
      NexusSettings.rwReleases,
      NexusSettings.rwSnapshots
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.mockito" % "mockito-core" % "1.9.5" % "test",
      "commons-io" % "commons-io" % "2.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.11.6" % "test"
    )
  )
}