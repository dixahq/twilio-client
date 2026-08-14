import sbt.Test

val scala2_13 = "2.13.18"

val Version = new AnyRef {
  val Pekko     = "1.3.0"
  val PekkoHttp = "1.3.0"

  // Jetty is not a direct dependency, it arrives transitively from wiremock-jetty12,
  // which imports jetty-bom and jetty-ee10-bom at 12.0.30. Pinned here to the patched
  // 12.0.36 and applied to every jetty module in dependencyOverrides below, because
  // jetty does not support mixing versions across its own modules.
  //
  // Deliberately stays on the 12.0.x line: wiremock 3.13.2 is the latest 3.x release
  // and has no 12.1.x build, so jetty must not be moved to 12.1.x here. That is what
  // the scala-steward:off marker guards, it is a line freeze and not a version floor,
  // so bumping within 12.0.x to pick up security patches is expected.
  val Jetty = "12.0.36" // scala-steward:off
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
        // Jetty, vulnerabilities from wiremock-jetty12 (Dependabot #50 high, #51, #52, #53).
        // #50 (GHSA-2fvj-hgj9-j2gr, jetty-security) reaches us only through
        // org.eclipse.jetty.ee10:jetty-ee10-servlet, a different group id, so overriding the
        // org.eclipse.jetty coordinates alone would not have closed it.
        // The whole stack is listed so it resolves to one consistent version, which is also
        // what a jetty-bom import would give us if sbt could import a BOM.
        "org.eclipse.jetty"       % "jetty-server"           % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-http"             % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-io"               % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-util"             % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-security"         % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-session"          % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-client"           % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-ee"               % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-xml"              % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-proxy"            % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-alpn-client"      % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-alpn-server"      % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-alpn-java-client" % Version.Jetty % Test,
        "org.eclipse.jetty"       % "jetty-alpn-java-server" % Version.Jetty % Test,
        "org.eclipse.jetty.ee10"  % "jetty-ee10-servlet"     % Version.Jetty % Test,
        "org.eclipse.jetty.ee10"  % "jetty-ee10-servlets"    % Version.Jetty % Test,
        "org.eclipse.jetty.ee10"  % "jetty-ee10-webapp"      % Version.Jetty % Test,
        "org.eclipse.jetty.http2" % "jetty-http2-common"     % Version.Jetty % Test,
        "org.eclipse.jetty.http2" % "jetty-http2-hpack"      % Version.Jetty % Test,
        "org.eclipse.jetty.http2" % "jetty-http2-server"     % Version.Jetty % Test,
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
