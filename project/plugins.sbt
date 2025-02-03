val mavenRepoHost = "repo.dixa.io"
resolvers += "Dixa repo" at s"https://$mavenRepoHost/content/repositories/releases/"

credentials += sys.env.get("CI").fold(Credentials(Path.userHome / ".sbt" / ".credentials")) { _ =>
  Credentials(
    realm = "Sonatype Nexus Repository Manager",
    host = mavenRepoHost,
    userName = sys.env("MAVEN_REPO_USER"),
    passwd = sys.env("MAVEN_REPO_PASSWORD")
  )
}
libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
// Internal plugins
addSbtPlugin("com.dixa" % "sbt-dixa-release" % "7.0.2")

// External plugins
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"         % "2.5.4")
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "5.1.0")
addDependencyTreePlugin
