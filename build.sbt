ThisBuild / organization := "io.github.matejcerny"
ThisBuild / scalaVersion := "2.12.20"

// Publishing settings for sbt-ci-release
ThisBuild / homepage := Some(url("https://github.com/matejcerny/sbt-config"))
ThisBuild / licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    id = "matejcerny",
    name = "Matej Cerny",
    email = "cerny.matej@gmail.com",
    url = url("https://matejcerny.cz/en/")
  )
)

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-config",
    description := "Configure sbt projects via HOCON configuration files",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.5" % Provided,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfatal-warnings"
    ),
    coverageExcludedFiles := ".*SbtConfigPlugin.*"
  )

lazy val docs = project
  .in(file("sbt-config-docs"))
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .settings(
    moduleName := "sbt-config-docs",
    mdocVariables := Map("VERSION" -> version.value),
    publish / skip := true
  )
  .dependsOn(root)
