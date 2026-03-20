import sbt.Test

val scala2_13 = "2.13.18"

val Version = new AnyRef {
  val Pekko     = "1.3.0"
  val PekkoHttp = "1.3.0"
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
  "17",
  "-Wconf:msg=discarding unmoored doc comment:s",
  "-Wconf:msg=Usages of trait.*will be easy to mistake:s",
)

lazy val `twilio-client` = project
  .in(file("."))
  .settings(
    Seq(
      organization := "com.dixa",
      scalaVersion := scala2_13,
      credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
      publishTo := {
        if (isSnapshot.value)
          Some("Dixa snapshots repo" at "https://repo.dixa.io/content/repositories/snapshots/")
        else
          Some("Dixa releases repo" at "https://repo.dixa.io/content/repositories/releases/")
      },
      scalacOptions     := scalacOpt,
      crossScalaVersions := Seq(scala2_13),
      libraryDependencies ++= Seq(
        // Pekko
        "org.apache.pekko" %% "pekko-actor-typed" % Version.Pekko     % Provided,
        "org.apache.pekko" %% "pekko-stream"      % Version.Pekko     % Provided,
        "org.apache.pekko" %% "pekko-http"        % Version.PekkoHttp % Provided,

        // Json serialization / deserialization
        "com.lihaoyi" %% "upickle" % "4.4.1",

        // Misc
        "com.neovisionaries" % "nv-i18n" % "1.29",

        // Lang improvement libs
        "com.beachape" %% "enumeratum" % "1.9.1",

        // Test
        "org.scalatest" %% "scalatest"        % "3.2.19" % Test,
        "org.scalamock" %% "scalamock"        % "7.5.2"  % Test,
        "org.wiremock"   % "wiremock-jetty12" % "3.13.2" % Test,
      ),
      dependencyOverrides ++= Seq(
        "commons-io"  % "commons-io" % "2.21.0" % Test,
        "net.minidev" % "json-smart" % "2.6.0"  % Test, // Vulnerability from wiremock
        // Vulnerability from wiremock
        "org.eclipse.jetty.http2" % "jetty-http2-common" % "12.0.29" % Test, // scala-steward:off
        "org.eclipse.jetty" % "jetty-server" % "12.0.32" % Test, // Vulnerability from wiremock
        "com.fasterxml.jackson.core" % "jackson-core" % "2.21.1" % Test // Vulnerability from wiremock
      ),
      publish / skip := false,

      // Test
      Test / compile := (Test / compile).dependsOn(Test / scalafmtCheckAll).value
    )
  )
