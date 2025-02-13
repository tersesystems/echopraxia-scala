import sbt.Keys._

val echopraxiaVersion            = "4.0.0"
val scalatestVersion             = "3.2.18"
val logbackClassicVersion        = "1.5.3"
val logstashVersion              = "8.0"
val enumeratumVersion            = "1.7.3"
val zjsonPatchVersion            = "0.4.16"
val sourceCodeVersion            = "0.3.1"
val refinedVersion               = "0.11.1"

val scala3                       = "3.6.2"
val scala213                     = "2.13.15"

val scalaVersions = List(scala3, scala213)

initialize := {
  val _        = initialize.value // run the previous initialization
  val required = "17"
  val current  = sys.props("java.specification.version")
  assert(current >= required, s"Unsupported JDK: java.specification.version $current != $required")
}

inThisBuild(
  Seq(
    scalaVersion := scala3,
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // only required for Scala 2.x
  )
)

ThisBuild / organization := "com.tersesystems.echopraxia.plusscala"
ThisBuild / homepage     := Some(url("https://github.com/tersesystems/echopraxia-plusscala"))

ThisBuild / startYear := Some(2021)
ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/tersesystems/echopraxia-plusscala"),
    "scm:git@github.com:tersesystems/echopraxia-plusscala.git"
  )
)

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / Compile / scalacOptions ++= optimizeInline

ThisBuild / Test / parallelExecution := false

Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)
Global / excludeLintKeys += ideSkipProject

lazy val api = (project in file("api"))
  .settings(
    name := "api",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    // Scala 3 doesn't need scala-reflect
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq.empty
        case other => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      }
    },
    //
    semanticdbEnabled := true, // enable SemanticDB
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "api"                % echopraxiaVersion,
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % sourceCodeVersion,
    // tests
    libraryDependencies += "eu.timepit"                 %% "refined"                  % refinedVersion        % Test,
    libraryDependencies += "com.beachape"               %% "enumeratum"               % enumeratumVersion     % Test,
    libraryDependencies += "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    // use logstash for testing
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )

lazy val logging = (project in file("logging"))
  .settings(
    name := "logging",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    libraryDependencies += "com.tersesystems.echopraxia" % "logging"                % echopraxiaVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
  )
  .dependsOn(api)

lazy val generic = (project in file("generic"))
  .settings(
    name := "generic",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => "com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.4"
        case other => "com.softwaremill.magnolia1_2" %% "magnolia" % "1.1.8"
      }
    },
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api, logger % "test")

lazy val simple = (project in file("simple"))
  .settings(
    name := "simple",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(logging)

lazy val logger = (project in file("logger"))
  .settings(
    name := "logger",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(api % "compile->compile;test->compile", logging)

lazy val flowLogger = (project in file("flow"))
  .settings(
    name := "flow-logger",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test
  )
  .dependsOn(logging % "compile->compile;test->compile")

// don't include dump for now
//lazy val dump = (project in file("dump"))
//  .settings(
//    name := "dump",
//    //
//    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"  % echopraxiaVersion % Test,
//    libraryDependencies += "org.scalatest"              %% "scalatest" % "3.2.12"      % Test
//  ).dependsOn(api % "compile->compile;test->compile")

lazy val traceLogger = (project in file("trace"))
  .settings(
    name := "trace-logger",
    crossScalaVersions := scalaVersions,
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    //
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash"                 % echopraxiaVersion     % Test,
    libraryDependencies += "ch.qos.logback"              % "logback-classic"          % logbackClassicVersion % Test,
    libraryDependencies += "net.logstash.logback"        % "logstash-logback-encoder" % logstashVersion       % Test,
    libraryDependencies += "org.scalatest"              %% "scalatest"                % scalatestVersion      % Test
  )
  .dependsOn(logging % "compile->compile;test->compile")

lazy val benchmarks = (project in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    scalacOptions := scalacOptionsVersion(scalaVersion.value),
    crossScalaVersions := scalaVersions,
    Compile / doc / sources                             := Seq.empty,
    Compile / packageDoc / publishArtifact              := false,
    publishArtifact                                     := false,
    publish / skip                                      := true,
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion
  )
  .dependsOn(api, logger, flowLogger, traceLogger)

lazy val root = (project in file("."))
  .settings(
    name                                   := "echopraxia-plusscala",
    Compile / doc / sources                := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    publishArtifact                        := false,
    publish / skip                         := true
  ).aggregate(
    api,
    logging,
    generic,
    logger,
    simple,
    flowLogger,
    traceLogger,
    benchmarks)

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) =>
      Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-encoding",
        "UTF-8",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Wunused:all",
        "-release",
        "8",
        "-explain"
      )
    case Some((2, n)) if n >= 13 =>
      Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-encoding",
        "UTF-8",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Yrangepos",
        "-Ytasty-reader",
        "-Wunused",
        "-release",
        "8",
        "-Vimplicits",
        "-Vtype-diffs",
        "-Xsource:3-cross",
        "-quickfix:cat=scala3-migration",

      )
  }
}

lazy val optimizeInline = Seq(
  "-opt:l:inline",
  "-opt-inline-from:echopraxia.plusscala.**",
  "-opt-warnings:any-inline-failed"
)
