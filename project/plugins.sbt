libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addDependencyTreePlugin
