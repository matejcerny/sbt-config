package cz.matejcerny.sbtconfig

import sbt._
import sbt.Keys._
import java.io.{File, PrintWriter}
import scala.util.Try

object SbtConfigPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val sbtConfigFile = settingKey[File]("The HOCON configuration file (default: build.conf)")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    sbtConfigFile := baseDirectory.value / "build.conf"
  ) ++ configSettings

  private def configSettings: Seq[Setting[_]] = Seq(
    name := configValue(sbtConfigFile, _.name).value.getOrElse(name.value),
    organization := configValue(sbtConfigFile, _.organization).value.getOrElse(organization.value),
    version := configValue(sbtConfigFile, _.version).value.getOrElse(version.value),
    scalaVersion := configValue(sbtConfigFile, _.scalaVersion).value.getOrElse(scalaVersion.value),
    scalacOptions ++= configValue(sbtConfigFile, _.scalacOptions).value.getOrElse(Seq.empty),
    libraryDependencies ++= configValue(sbtConfigFile, _.dependencies).value
      .getOrElse(Seq.empty)
      .map(toModuleId),
    libraryDependencies ++= configValue(sbtConfigFile, _.testDependencies).value
      .getOrElse(Seq.empty)
      .map(d => toModuleId(d) % Test)
  )

  private def configValue[A](
    fileKey: SettingKey[File],
    extract: ProjectConfig => Option[A]
  ): Def.Initialize[Option[A]] = Def.setting {
    val file = fileKey.value
    ensureConfigFileExists(file)
    loadConfig(file).flatMap(extract)
  }

  private def loadConfig(file: File): Option[ProjectConfig] = {
    ConfigParser.parse(file) match {
      case Right(config) => Some(config)
      case Left(error) =>
        System.err.println(s"[sbt-config] $error")
        None
    }
  }

  private def ensureConfigFileExists(file: File): Unit = {
    if (!file.exists()) {
      createDefaultConfigFile(file)
    }
  }

  private def createDefaultConfigFile(file: File): Unit = {
    val content =
      s"""# SBT project configuration
         |# Uncomment and modify the settings you want to use
         |
         |# name = "${ProjectConfig.Example.name}"
         |# organization = "${ProjectConfig.Example.organization}"
         |# version = "${ProjectConfig.Example.version}"
         |# scalaVersion = "${ProjectConfig.Example.scalaVersion}"
         |
         |# scalacOptions = [
         |#   "-deprecation",
         |#   "-feature",
         |#   "-unchecked"
         |# ]
         |
         |# dependencies = [
         |#   "org.typelevel:cats-core:2.13.0"
         |# ]
         |
         |# testDependencies = [
         |#   "org.scalatest:scalatest:3.2.19"
         |# ]
         |""".stripMargin

    val _ = Try {
      val writer = new PrintWriter(file)
      Try(writer.write(content)).foreach(_ => writer.close())
    }
  }

  private def toModuleId(dep: Dependency): ModuleID = {
    if (dep.crossVersion) {
      dep.organization %% dep.name % dep.version
    } else {
      dep.organization % dep.name % dep.version
    }
  }
}
