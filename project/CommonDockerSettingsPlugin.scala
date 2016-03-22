
import com.typesafe.sbt.packager.docker.{ExecCmd, Cmd, DockerKeys}
import sbt._

object CommonDockerSettingsPlugin extends AutoPlugin with DockerKeys {
  override def trigger = allRequirements
  override def requires = com.typesafe.sbt.packager.docker.DockerPlugin
  override lazy val projectSettings = Seq(
    dockerBaseImage := "givee-test.qiwi.com:5000/java8:latest",
    dockerRepository := Some("givee-test.qiwi.com:5000"),
    dockerCommands := Seq(
      Cmd("FROM", dockerBaseImage.value),
      Cmd("MAINTAINER", "Denis Mikhaylov <d.mikhaylov@qiwi.ru>"),
      Cmd("WORKDIR", "/opt/docker"),
      Cmd("COPY", "opt /opt")
    ) ++
      makeExpose(dockerExposedPorts.value) ++
      makeVolumes(dockerExposedVolumes.value) ++
      Seq(
        ExecCmd("ENTRYPOINT", dockerEntrypoint.value: _*),
        ExecCmd("CMD", dockerCmd.value: _*)
      )
  )
  def makeExpose(ports: Seq[Int]) = {
    if (ports.isEmpty) Seq.empty[Cmd] else Seq(Cmd("EXPOSE", ports.mkString(", ")))
  }
  def makeVolumes(volumes: Seq[String]) = {
    volumes.map(volume =>  Cmd("VOLUME", volume))
  }
}