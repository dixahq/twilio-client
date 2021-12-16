import sbt.Test

val scala2_12          = "2.12.14"
val releasesRepository = "Dixa repo" at "https://repo.dixa.io/content/repositories/releases/"
val snapshotsRepository =
  "Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/"
val twitterHttpsRepo   = "Twitter Repository https" at "https://maven.twttr.com/"
val confluentHttpsRepo = "confluent.io" at "https://packages.confluent.io/maven/"

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
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-target:jvm-1.8"
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
      "org.http4s"                   %% "http4s-blaze-client" % Version.Http4s,
      "org.http4s"                   %% "http4s-circe"        % Version.Http4s,
      "org.http4s"                   %% "http4s-client"       % Version.Http4s,
      "org.http4s"                   %% "http4s-core"         % Version.Http4s,
      "org.typelevel"                %% "cats-effect"         % Version.CatsEffect,
      "io.circe"                     %% "circe-core"          % Version.Circe,
      "io.circe"                     %% "circe-generic"       % Version.Circe,
      "io.circe"                     %% "circe-parser"        % Version.Circe,
      "com.beachape"                 %% "enumeratum"          % "1.7.0",
      "com.dixa"                     %% "thrift"              % Version.Protocols,
      "com.twilio.sdk"                % "twilio-java-sdk"     % "6.3.0",
      "com.googlecode.libphonenumber" % "libphonenumber"      % "8.12.39",
      "com.dixa"                     %% "thirdparty-library"  % "2.1.4",
      "com.dixa"                     %% "testutil"            % "1.3.3"           % Test,
      "org.scalatest"                %% "scalatest"           % Version.Scalatest % Test
    ),
      Test / compile := (Test / compile).dependsOn(Test / scalafmtCheckAll).value
    )
  )
  .enablePlugins(
    JavaAppPackaging,
    UniversalDeployPlugin
  )
