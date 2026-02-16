ThisBuild / organization := "io.github.matejcerny"
ThisBuild / scalaVersion := "2.12.20"
ThisBuild / crossScalaVersions := Seq("2.12.20", "3.8.1")

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
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.3"
        case _      => "2.0.0-RC9"
      }
    },
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
      "-unchecked"
    ),
    scalacOptions ++= {
      if (scalaBinaryVersion.value == "2.12")
        Seq("-Xfatal-warnings", "-Xlint", "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-value-discard")
      else Seq("-Werror", "-Wconf:msg=deprecated for wildcard arguments:s")
    },
    coverageExcludedFiles := ".*SbtConfigPlugin.*"
  )

lazy val docs = project
  .in(file("sbt-config-docs"))
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .settings(
    moduleName := "sbt-config-docs",
    crossScalaVersions := Seq("2.12.20"),
    mdocVariables := Map(
      "VERSION" -> dynverGitDescribeOutput.value
        .map(_.ref.dropPrefix)
        .getOrElse(version.value)
    ),
    publish / skip := true
  )
  .dependsOn(root)
