import sbt.Test

val scala2_12          = "2.12.15"
val releasesRepository = "Dixa repo" at "https://repo.dixa.io/content/repositories/releases/"
val snapshotsRepository =
  "Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/"
val twitterHttpsRepo   = "Twitter Repository https" at "https://maven.twttr.com/"
val confluentHttpsRepo = "confluent.io" at "https://packages.confluent.io/maven/"

val Version = new AnyRef {
  val Akka     = "2.6.19"
  val AkkaHttp = "10.2.9"
  val Circe    = "0.14.2"

  // test
  val ScalatestScalactic = "3.2.13"
}

lazy val `twilio-client` = project
  .in(file("."))
  .settings(
    Seq(
      organization := "com.dixa",
      scalaVersion := scala2_12,
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
      scalacOptions := Seq(
        "-feature",
        "-unchecked",
        "-deprecation",
        "-encoding",
        "utf8",
        "-Xlint",
        "-Xfatal-warnings",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-target:jvm-1.8",
        "-Wconf:msg=discarding unmoored doc comment:s"
      ),
      crossScalaVersions := Seq(scala2_12),
      releaseCrossBuild  := true,
      publishTo := {
        if (version.value.trim.endsWith("-SNAPSHOT")) {
          Some(snapshotsRepository)
        } else {
          Some(releasesRepository)
        }
      },
      libraryDependencies ++= Seq(
        // Akka
        "com.typesafe.akka" %% "akka-actor-typed" % Version.Akka,
        "com.typesafe.akka" %% "akka-stream"      % Version.Akka,
        "com.typesafe.akka" %% "akka-http"        % Version.AkkaHttp,

        // Circe
        "io.circe" %% "circe-core"    % Version.Circe,
        "io.circe" %% "circe-generic" % Version.Circe,
        "io.circe" %% "circe-parser"  % Version.Circe,

        // Misc
        "com.neovisionaries" % "nv-i18n" % "1.29",

        // Lang improvement libs
        "org.scalactic" %% "scalactic"  % Version.ScalatestScalactic,
        "com.beachape"  %% "enumeratum" % "1.7.0",

        // Test
        "org.scalatest" %% "scalatest"                   % Version.ScalatestScalactic % Test,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0"                    % Test,
        "com.github.tomakehurst" % "wiremock" % "2.27.2" % Test
      ),
      coverageMinimumStmtTotal := 75,
      coverageFailOnMinimum    := false,
      coverageHighlighting     := false,
      Test / compile           := (Test / compile).dependsOn(Test / scalafmtCheckAll).value
    )
  )
  .enablePlugins(
    JavaAppPackaging,
    UniversalDeployPlugin
  )
