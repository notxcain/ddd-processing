resolvers ++= Seq(
  "QIWI Snapshots Repo" at "https://maven.osmp.ru/nexus/content/repositories/qiwiweb-snapshots",
  "QIWI Releases Repo" at "https://maven.osmp.ru/nexus/content/repositories/qiwiweb-releases"
)
addSbtPlugin("com.qiwi" % "qiwi-sbt-plugin" % "2.4.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")
addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.6")