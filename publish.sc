#!/usr/bin/env -S scala-cli shebang

//> using scala 3
//> using dep com.lihaoyi::requests::0.9.0
//> using dep com.lihaoyi::upickle::4.4.1

import java.util.UUID
import java.nio.file.{Files, Paths}
import sys.process.*

val versionSbt = Paths.get("version.sbt")

def readVersionSbt(): String =
  Files.readString(versionSbt)

def writeVersionSbt(content: String): Unit =
  Files.writeString(versionSbt, content)

def extractVersion(content: String): String =
  """ThisBuild / version := "(.+)"""".r
    .findFirstMatchIn(content)
    .map(_.group(1))
    .getOrElse(sys.error("Cannot parse version from version.sbt"))

def prompt(question: String, valid: String*): String =
  var answer = ""
  while answer.isEmpty do
    print(question)
    val input = scala.io.StdIn.readLine().trim
    if valid.isEmpty || valid.contains(input) then answer = input
    else println(s"Please enter one of: ${valid.mkString(", ")}")
  answer

val originalContent = readVersionSbt()
val originalVersion  = extractVersion(originalContent)

val mode = prompt("Snapshot or release? [s/r]: ", "s", "r")

mode match
  case "s" =>
    if !originalVersion.endsWith("SNAPSHOT") then
      sys.error(s"Current version '$originalVersion' is not a SNAPSHOT version")

    val snapshotVersion = originalVersion.replace("SNAPSHOT", s"${UUID.randomUUID()}-SNAPSHOT")
    println(s"Publishing snapshot: $snapshotVersion")
    writeVersionSbt(s"""ThisBuild / version := "$snapshotVersion"\n""")

    val code =
      try Process(Seq("sbt", "+test", "+publish")).!
      finally writeVersionSbt(originalContent)

    if code != 0 then sys.exit(code)
    println(s"\nPublished: $snapshotVersion")

  case "r" =>
    val bump = prompt("Bump major, minor, or patch? [major/minor/patch]: ", "major", "minor", "patch")

    print("Fetching latest tag from GitHub... ")
    val resp = requests.get(
      "https://api.github.com/repos/dixahq/twilio-client/tags",
      params  = Map("per_page" -> "100"),
      headers = Map("User-Agent" -> "twilio-client-publish-script"),
    )
    val vRe      = raw"v(\d+)\.(\d+)\.(\d+)".r
    val versions = ujson.read(resp.text()).arr.toSeq
      .flatMap(t => vRe.findFirstMatchIn(t("name").str))
      .map(m => (m.group(1).toInt, m.group(2).toInt, m.group(3).toInt))

    if versions.isEmpty then sys.error("No version tags found on GitHub")

    val (latMaj, latMin, latPat) = versions.maxBy(identity)
    println(s"v$latMaj.$latMin.$latPat")

    val (newMaj, newMin, newPat) = bump match
      case "major" => (latMaj + 1, 0, 0)
      case "minor" => (latMaj, latMin + 1, 0)
      case "patch" => (latMaj, latMin, latPat + 1)

    val newVersion = s"$newMaj.$newMin.$newPat"
    println(s"Publishing release: $newVersion")
    writeVersionSbt(s"""ThisBuild / version := "$newVersion"\n""")

    val code =
      try Process(Seq("sbt", "+test", "+publish")).!
      finally writeVersionSbt(originalContent)

    if code != 0 then sys.exit(code)

    val tagCode = Process(Seq("gh", "release", "create", s"v$newVersion", "--title", s"v$newVersion", "--notes", "")).!
    if tagCode != 0 then
      System.err.println(s"\nWarning: artifact published but failed to create GitHub release tag v$newVersion.")
      System.err.println(s"Create it manually with: gh release create v$newVersion --title v$newVersion --notes \"\"")

    println(s"\nPublished: $newVersion")
