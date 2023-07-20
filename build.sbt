import sbt.Test
import com.dixa.sbt.ReleaseStateTransformations.{
  dixaAddGitHistoryToReleaseTag,
  dixaCheckSnapshotDependencies,
  dixaDetermineVersion,
  dixaPushReleaseTag
}
import sbtrelease.ReleasePlugin.autoImport.{
  releaseProcess,
  releaseStepCommandAndRemaining,
  ReleaseStep
}
import sbtrelease.ReleaseStateTransformations.{runClean, tagRelease}

val scala2_12          = "2.12.18"
val scala2_13          = "2.13.11"
val releasesRepository = "Dixa repo" at "https://repo.dixa.io/content/repositories/releases/"
val snapshotsRepository =
  "Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/"
val twitterHttpsRepo   = "Twitter Repository https" at "https://maven.twttr.com/"
val confluentHttpsRepo = "confluent.io" at "https://packages.confluent.io/maven/"

val Version = new AnyRef {
  val Akka     = "2.6.20"
  val AkkaHttp = "10.2.10"
  val Circe    = "0.14.5"
}

val scalacOpt = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Xlint",
  "-Xfatal-warnings",
  "-release",
  "8",
  "-Wconf:msg=discarding unmoored doc comment:s"
)

lazy val `twilio-client` = project
  .in(file("."))
  .settings(
    Seq(
      organization := "com.dixa",
      scalaVersion := scala2_13,
      resolvers ++= Seq(
        releasesRepository,
        twitterHttpsRepo,
        snapshotsRepository,
        confluentHttpsRepo
      ),
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
      },
      scalacOptions := {
        if (scalaVersion.value == scala2_12)
          scalacOpt :+ "-Wconf:cat=unused-params:s"
        else
          scalacOpt
      },
      crossScalaVersions := Seq(scala2_12, scala2_13),
      releaseCrossBuild  := true,
      libraryDependencies ++= Seq(
        // Akka
        "com.typesafe.akka" %% "akka-actor-typed" % Version.Akka     % Provided,
        "com.typesafe.akka" %% "akka-stream"      % Version.Akka     % Provided,
        "com.typesafe.akka" %% "akka-http"        % Version.AkkaHttp % Provided,

        // Json serialization / deserialization
        "com.lihaoyi" %% "upickle" % "3.1.2",

        // Misc
        "com.neovisionaries" % "nv-i18n" % "1.29",

        // Lang improvement libs
        "com.beachape" %% "enumeratum" % "1.7.2",

        // Test
        "org.scalatest"         %% "scalatest" % "3.2.16"        % Test,
        "org.scalamock"         %% "scalamock" % "5.2.0"         % Test,
        "com.github.tomakehurst" % "wiremock"  % "3.0.0-beta-10" % Test,
      ),
      dependencyOverrides ++= Seq(
        // Dependencies added to handle vulnerabilities in transitive Test and Provided dependencies
        "org.eclipse.jetty.http2"          % "http2-server"            % "11.0.14", // From Wiremock
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.15.1",  // From Wiremock
      ),
      publish / skip := false,
      releaseProcess :=
        Seq[ReleaseStep](
          dixaCheckSnapshotDependencies,
          runClean,
          dixaDetermineVersion,
          releaseStepCommandAndRemaining("+test"),
          releaseStepCommandAndRemaining("+publish"),
          tagRelease,
          dixaAddGitHistoryToReleaseTag,
          dixaPushReleaseTag
        ),

      // Test
      Test / compile := (Test / compile).dependsOn(Test / scalafmtCheckAll).value
    )
  )
