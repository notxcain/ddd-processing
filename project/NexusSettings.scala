import sbt._

object NexusSettings {
  val realm = "Sonatype Nexus Repository Manager"
  val host = "maven.osmp.ru"
  def readWriteUrlFor(content: String) = s"https://$host/${readWritePathTo(content)}"
  def readOnlyURLFor(content: String) = s"https://$host/${readOnlyPathTo(content)}"
  def readWritePathTo(content: String) = s"nexus/content/repositories/qiwiweb-$content/"
  def readOnlyPathTo(content: String) = s"nexus/content/repositories/$content/"

  val rwSnapshots = "Read/Write QIWI Snapshots Repo" at NexusSettings.readWriteUrlFor("snapshots")
  val rwReleases = "Read/Write QIWI Releases Repo" at  NexusSettings.readWriteUrlFor("releases")
}