import ReleaseTransformations._

organization := "com.dixa"
name := "conversation-service"
version := "0.0.1"
scalaVersion := "2.12.10"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8",
  "-language:postfixOps",
  "-target:jvm-1.8",
  "-Xlint",
)


enablePlugins(JavaAppPackaging)
mainClass in Compile := Some("com.dixa.conversationservice.init.Boot")
executableScriptName := "start"

lazy val dixaRepo = "Dixa repo" at "https://repo.dixa.io/content/repositories/releases/"
lazy val dixaSnapshotRepo = "Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/"
lazy val twitterRepo = "Twitter repo" at "https://repo.dixa.io/content/repositories/twitter/"
lazy val confluentRepository = "confluent.io" at "http://packages.confluent.io/maven/"

resolvers ++= Seq(
  dixaRepo,
  dixaSnapshotRepo,
  twitterRepo,
  confluentRepository,
  Resolver.bintrayRepo("giflw", "maven")
)

// Credentials for repo.dixa.io
credentials += sys.env.get("CI").map { _ =>
  // Running on CI, so grab credentials from ENV
  Credentials(
    realm = "Sonatype Nexus Repository Manager",
    host = "repo.dixa.io",
    userName = sys.env("MAVEN_REPO_USER"),
    passwd = sys.env("MAVEN_REPO_PASSWORD")
  )
} getOrElse {
  Credentials(Path.userHome / ".sbt" / ".credentials")
}

lazy val protocolsVersion = "3.0.52"

libraryDependencies ++= Seq(
  "com.dixa" %% "dynamoutil" % "0.9.73",
  "com.dixa" %% "thrift" % protocolsVersion,
  "com.dixa" %% "json-api" % protocolsVersion,
  "com.dixa" %% "thirdparty-server" % "2.1.6",
  "com.dixa" %% "thirdparty-library" % "2.1.6",
  "com.typesafe.akka" %% "akka-stream" % "2.5.25",
  "com.dixa" %% "appserver" % "1.0.3",
  "com.dixa" %% "threadfactory" % "1.0.1",
  "com.dixa" %% "thrift-avro-converter" % "1.0.9",
  "com.dixa" %% "domain-event-publisher" % "1.0.47" exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "com.dixa" %% "testutil" % "1.3.5" % "test",
)

javaOptions in Universal ++= Seq(
  "-Dcom.sun.management.jmxremote",
  "-Dcom.sun.management.jmxremote.authenticate=false",
  "-Dcom.sun.management.jmxremote.ssl=false",
  "-Dcom.sun.management.jmxremote.local.only=false",
  "-Dcom.sun.management.jmxremote.port=1099",
  "-Dcom.sun.management.jmxremote.rmi.port=1099",
  "-Djava.rmi.server.hostname=127.0.0.1"
)

coverageMinimum := 50
coverageFailOnMinimum := false
coverageHighlighting := false


releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  releaseStepCommand("stage")
)
