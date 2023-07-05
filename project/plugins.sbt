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
addSbtPlugin("com.dixa"      % "sbt-dixa-release" % "6.2.23")
addSbtPlugin("org.scalameta" % "sbt-scalafmt"     % "2.5.0")
addDependencyTreePlugin
