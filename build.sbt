ThisBuild / organization := "io.github.matejcerny"
ThisBuild / scalaVersion := "2.12.20"
ThisBuild / crossScalaVersions := Seq("2.12.20", "3.8.2")

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
        case "2.12" => "1.12.8"
        case _      => "2.0.0-RC10"
      }
    },
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.6" % Provided,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
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
    coverageExcludedFiles := ".*SbtConfigPlugin.*",
    Compile / doc / scalacOptions ++= {
      if (scalaBinaryVersion.value == "3")
        Seq(
          "-project", "sbt-config",
          "-project-version", dynverGitDescribeOutput.value
            .map(_.ref.dropPrefix)
            .getOrElse(version.value),
          "-siteroot", "docs",
          "-social-links:github::https://github.com/matejcerny/sbt-config",
          "-project-logo", "docs/_assets/images/logo.svg",
          "-project-footer", "Copyright Matej Cerny",
          "-versions-dictionary-url", "https://matejcerny.github.io/sbt-config/versions.json"
        )
      else Nil
    },
    Compile / doc := {
      val output = (Compile / doc).value
      val assetsDir = (ThisBuild / baseDirectory).value / "docs" / "_assets"
      val favicon = assetsDir / "images" / "favicon.ico"
      if (favicon.exists()) IO.copyFile(favicon, output / "favicon.ico")
      val customCss = assetsDir / "css" / "custom.css"
      if (customCss.exists()) IO.copyFile(customCss, output / "styles" / "staticsitestyles.css")
      val hoconJs = assetsDir / "js" / "hljs-hocon.js"
      val targetJs = output / "scripts" / "hljs-scala3.js"
      if (hoconJs.exists() && targetJs.exists()) {
        val existing = IO.read(targetJs)
        IO.write(targetJs, existing + "\n" + IO.read(hoconJs))
      }
      output
    }
  )
