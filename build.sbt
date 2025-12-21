ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "cz.matejcerny"
ThisBuild / scalaVersion := "2.12.20"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-config",
    description := "Configure sbt projects via HOCON configuration files",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.3"
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
    )
  )
