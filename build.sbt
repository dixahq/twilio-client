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
      scalacOptions      := scalacOpt,
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
        "org.eclipse.jetty" % "jetty-server" % "12.0.33" % Test, // Vulnerability from wiremock
        "org.eclipse.jetty" % "jetty-http" % "12.0.33" % Test, // GHSA-355h-qmc2-wpwf (transitive via wiremock-jetty12)
        // jackson 2 suite, vulnerabilities from wiremock (Dependabot #43-#46, transitive via wiremock-jetty12)
        "com.fasterxml.jackson.core" % "jackson-core"     % "2.22.0" % Test,
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.22.0" % Test
        // NOTE: Dependabot #42 (GHSA-r4gv-qr8j-p3pg, handlebars < 4.5.2) is intentionally NOT
        // overridden here. Forcing com.github.jknack:handlebars(-helpers):4.5.2 breaks WireMock
        // 3.13.2's response templating: 4.5.2 relocated the helper classes into a new `.ext`
        // subpackage (e.g. com.github.jknack.handlebars.helper.ext.NumberHelper), but WireMock's
        // TemplateEngine.addHelpers() still references the old package and throws
        // NoClassDefFoundError: com/github/jknack/handlebars/helper/NumberHelper at test startup.
        // WireMock 3.13.2 is the latest release and pins handlebars 4.3.1 transitively; no WireMock
        // version is compatible with handlebars >= 4.5.2 yet. handlebars is Test-scope only.
        // Revisit when WireMock ships a build compatible with handlebars 4.5.2.
      ),
      publish / skip := false,

      // Test
      Test / compile := (Test / compile).dependsOn(Test / scalafmtCheckAll).value
    )
  )
