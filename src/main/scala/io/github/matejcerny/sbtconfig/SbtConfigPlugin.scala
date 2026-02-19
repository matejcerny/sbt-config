package io.github.matejcerny.sbtconfig

import io.github.matejcerny.sbtconfig.model._
import io.github.matejcerny.sbtconfig.parser.ConfigParser
import sbt.{ Developer => _, License => _, _ }
import sbt.Keys._
import java.io.{ File, PrintWriter }
import scala.util.Try

object SbtConfigPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val sbtConfigFile = settingKey[File]("The HOCON configuration file (default: build.conf)")
  }

  import autoImport._

  // Key with the same label as sbt-platform-deps' platformDepsCrossVersion.
  // When a platform plugin (sbt-scalajs, sbt-scala-native) is present,
  // it overrides this in projectSettings to include the platform suffix,
  // giving js/native dependencies the equivalent of %%% behavior.
  private val platformDepsCrossVersion = settingKey[CrossVersion](
    "The cross version used by %%% for platform-specific dependencies"
  )

  override def globalSettings: Seq[Setting[_]] = Seq(
    platformDepsCrossVersion := CrossVersion.binary
  )

  override def projectSettings: Seq[Setting[_]] = Seq(
    sbtConfigFile := baseDirectory.value / "build.conf"
  ) ++ configSettings

  private def configSettings: Seq[Setting[_]] = Seq(
    name := configValue(sbtConfigFile, _.name).value.getOrElse(name.value),
    organization := configValue(sbtConfigFile, _.organization).value.getOrElse(organization.value),
    version := configValue(sbtConfigFile, _.version).value.getOrElse(version.value),
    scalaVersion := configValue(sbtConfigFile, _.scalaVersion).value.getOrElse(scalaVersion.value),
    scalacOptions ++= configValue(sbtConfigFile, _.scalacOptions).value.getOrElse(Seq.empty),
    libraryDependencies ++= {
      val deps = configValue(sbtConfigFile, _.dependencies).value.getOrElse(Seq.empty)
      val platformCV = platformDepsCrossVersion.value
      deps.map(toModuleId(_, platformCV))
    },
    libraryDependencies ++= {
      val deps = configValue(sbtConfigFile, _.testDependencies).value.getOrElse(Seq.empty)
      val platformCV = platformDepsCrossVersion.value
      deps.map(toModuleId(_, platformCV) % Test)
    },
    homepage := configValue(sbtConfigFile, _.homepage).value.map(url) orElse homepage.value,
    licenses ++= configValue(sbtConfigFile, _.licenses).value
      .getOrElse(Seq.empty)
      .flatMap(License.toLicense),
    versionScheme := configValue(sbtConfigFile, _.versionScheme).value orElse versionScheme.value,
    developers ++= configValue(sbtConfigFile, _.developers).value
      .getOrElse(Seq.empty)
      .map(toDeveloper)
      .toList
  )

  // Cache parsed configs by file path to avoid reparsing and duplicate warnings
  private val configCache = scala.collection.mutable.Map[String, Option[ProjectConfig]]()

  private def configValue[A](
      fileKey: SettingKey[File],
      extract: ProjectConfig => Option[A]
  ): Def.Initialize[Option[A]] = Def.setting {
    val file = fileKey.value
    ensureConfigFileExists(file)
    loadConfig(file).flatMap(extract)
  }

  private def loadConfig(file: File): Option[ProjectConfig] =
    configCache.getOrElseUpdate(
      file.getAbsolutePath,
      ConfigParser.parse(file) match {
        case Right(config) => Some(config)
        case Left(error) =>
          System.err.println(s"[sbt-config] $error")
          None
      }
    )

  private def ensureConfigFileExists(file: File): Unit =
    if (!file.exists()) {
      createDefaultConfigFile(file)
    }

  private def createDefaultConfigFile(file: File): Unit = {
    val knownLicensesList = License.supported.sorted.mkString(", ")
    val content =
      s"""# sbt-config: HOCON configuration for sbt projects
         |# Documentation: https://matejcerny.github.io/sbt-config/
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
         |# Dependencies (format: "organization:artifact:version")
         |# Flat list — all use Scala cross-versioning (%%)
         |# dependencies = [
         |#   "org.typelevel:cats-core:2.13.0"
         |# ]
         |#
         |# Nested object — specify dependency type
         |# dependencies {
         |#   scala  = ["org.typelevel:cats-core:2.13.0"]   # %%
         |#   java   = ["com.google.code.gson:gson:2.11.0"] # %
         |#   js     = ["org.scala-js:scalajs-dom:2.8.0"]   # %%% (Scala.js)
         |#   native = ["com.armanbilge:epollcat:0.1.6"]     # %%% (Scala Native)
         |# }
         |
         |# Test dependencies (automatically added with Test scope)
         |# testDependencies = [
         |#   "org.scalatest:scalatest:3.2.19"
         |# ]
         |
         |# Publishing settings (requires sbt-ci-release plugin)
         |# homepage = "${ProjectConfig.Example.homepage}"
         |# licenses = ["MIT"]  # Supported: $knownLicensesList
         |# versionScheme = "${ProjectConfig.Example.versionScheme}"  # Options: early-semver, semver-spec, pvp, always, strict
         |# developers = [
         |#   { id = "johndoe", name = "John Doe", email = "john@example.com", url = "https://example.com" }
         |# ]
         |""".stripMargin

    val _ = Try {
      val writer = new PrintWriter(file)
      Try(writer.write(content)).foreach(_ => writer.close())
    }
  }

  private def toModuleId(dep: Dependency, platformCV: CrossVersion): ModuleID =
    dep.crossVersionType match {
      case CrossVersionType.Java => dep.organization % dep.name % dep.version
      case CrossVersionType.ScalaJs | CrossVersionType.ScalaNative =>
        (dep.organization % dep.name % dep.version).cross(platformCV)
      case _ => dep.organization %% dep.name % dep.version
    }

  private def toDeveloper(dev: Developer): sbt.Developer =
    sbt.Developer(
      id = dev.id,
      name = dev.name,
      email = dev.email,
      url = url(dev.url)
    )
}
