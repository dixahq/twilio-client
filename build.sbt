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

val scala2_13          = "2.13.12"
val releasesRepository = "Dixa repo" at "https://repo.dixa.io/content/repositories/releases/"
val snapshotsRepository =
  "Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/"
val twitterHttpsRepo   = "Twitter Repository https" at "https://maven.twttr.com/"
val confluentHttpsRepo = "confluent.io" at "https://packages.confluent.io/maven/"

val Version = new AnyRef {
  val Pekko     = "1.0.1"
  val PekkoHttp = "1.0.0"
  val Circe     = "0.14.5"
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
      scalacOptions      := scalacOpt,
      crossScalaVersions := Seq(scala2_13),
      releaseCrossBuild  := true,
      libraryDependencies ++= Seq(
        // Pekko
        "org.apache.pekko" %% "pekko-actor-typed" % Version.Pekko     % Provided,
        "org.apache.pekko" %% "pekko-stream"      % Version.Pekko     % Provided,
        "org.apache.pekko" %% "pekko-http"        % Version.PekkoHttp % Provided,

        // Json serialization / deserialization
        "com.lihaoyi" %% "upickle" % "3.1.4",

        // Misc
        "com.neovisionaries" % "nv-i18n" % "1.29",

        // Lang improvement libs
        "com.beachape" %% "enumeratum" % "1.7.3",

        // Test
        "org.scalatest" %% "scalatest" % "3.2.17" % Test,
        "org.scalamock" %% "scalamock" % "5.2.0"  % Test,
        "org.wiremock"   % "wiremock"  % "3.4.2"  % Test,
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
