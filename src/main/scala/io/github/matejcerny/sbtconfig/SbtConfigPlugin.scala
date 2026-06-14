package io.github.matejcerny.sbtconfig

import io.github.matejcerny.sbtconfig.model._
import io.github.matejcerny.sbtconfig.parser.ConfigParser
import sbt.{ Developer => _, License => _, Resolver => _, _ }
import sbt.Keys._
import java.io.{ File, PrintWriter }
import scala.util.Try

object SbtConfigPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val sbtConfigFile = settingKey[File]("The HOCON configuration file (default: build.conf)")
    val sbtConfigPlatform =
      settingKey[Platform]("The platform of the current project (auto-detected from platform plugins)")
    val sbtConfigModule =
      settingKey[Option[String]]("Override the module key this project binds to (default: derived from project id)")
    val Platform = model.Platform
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
    sbtConfigFile := (LocalRootProject / baseDirectory).value / "build.conf",
    sbtConfigModule := None,
    sbtConfigPlatform := detectPlatform(platformDepsCrossVersion.value)
  ) ++ configSettings

  private def configSettings: Seq[Setting[_]] = Seq(
    name := configValue(_.name).value.getOrElse(name.value),
    organization := configValue(_.organization).value.getOrElse(organization.value),
    version := configValue(_.version).value.getOrElse(version.value),
    scalaVersion := configValue(_.scalaVersion).value.getOrElse(scalaVersion.value),
    scalacOptions ++= configValue(_.scalacOptions).value.getOrElse(Seq.empty),
    libraryDependencies ++= {
      val deps = configValue(_.dependencies).value.getOrElse(Seq.empty)
      val platform = sbtConfigPlatform.value
      val platformCV = platformDepsCrossVersion.value
      filterDeps(deps, platform).map(toModuleId(_, platformCV))
    },
    libraryDependencies ++= {
      val deps = configValue(_.testDependencies).value.getOrElse(Seq.empty)
      val platform = sbtConfigPlatform.value
      val platformCV = platformDepsCrossVersion.value
      filterDeps(deps, platform).map(toModuleId(_, platformCV) % Test)
    },
    homepage := configValue(_.homepage).value.map(url) orElse homepage.value,
    licenses ++= configValue(_.licenses).value
      .getOrElse(Seq.empty)
      .flatMap(License.toLicense),
    versionScheme := configValue(_.versionScheme).value orElse versionScheme.value,
    developers ++= configValue(_.developers).value
      .getOrElse(Seq.empty)
      .map(toDeveloper)
      .toList,
    resolvers ++= configValue(_.resolvers).value
      .getOrElse(Seq.empty)
      .map(toResolver)
  )

  // Cache parsed BuildConfigs to avoid reparsing and duplicate per-file diagnostics within a resolution pass.
  // Keyed by file path *and* its modification signature (lastModified, length) so that a reload after the file
  // changes reparses instead of returning a stale config — e.g. a now-malformed file must abort, not silently
  // keep the previously-loaded settings.
  private val configCache = scala.collection.mutable.Map[(String, Long, Long), BuildConfig]()

  // Per-id de-dup for the unlisted-project warning (the file-keyed cache can't dedup this — all projects share one file).
  private val warnedUnlisted = scala.collection.mutable.Set[String]()

  // Extract a single field from the resolved config. Field-typed (not Option[ProjectConfig]) so the consuming
  // setting's cache inputs stay hashable on sbt 2; resolvedConfig is composed in, never exposed via `.value` directly.
  private def configValue[A](extract: ProjectConfig => Option[A]): Def.Initialize[Option[A]] =
    Def.setting(resolvedConfig.value.flatMap(extract))

  // Resolve the config that applies to the current project, on top of the cached BuildConfig.
  // Single-project mode (no modules) -> shared applies to everyone. Multi-module mode -> merge shared + the matched
  // module, or None (with a one-time warning) when the project's id is not listed. Not cached: it varies per project id.
  private def resolvedConfig: Def.Initialize[Option[ProjectConfig]] = Def.setting {
    val file = sbtConfigFile.value
    ensureConfigFileExists(file)
    val bc = loadConfig(file)
    val id = thisProject.value.id
    val rootId = (LocalRootProject / thisProject).value.id

    if (bc.modules.isEmpty) {
      Some(bc.shared)
    } else {
      val key = sbtConfigModule.value.orElse(ModuleResolver.resolveKey(id, bc.modules.keySet))
      key.flatMap(k => bc.modules.get(k).map(m => ModuleResolver.merge(bc.shared, m, k))) match {
        case some @ Some(_) => some
        case None =>
          if (id != rootId && warnedUnlisted.add(id))
            System.err.println(s"[sbt-config] project `$id` is not configured by sbt-config — no `modules.$id` entry")
          None
      }
    }
  }

  private def loadConfig(file: File): BuildConfig =
    configCache.getOrElseUpdate(
      (file.getAbsolutePath, file.lastModified(), file.length()),
      ConfigParser.parse(file) match {
        case Right(cfg) =>
          if (cfg.shared.name.isDefined && cfg.modules.nonEmpty)
            System.err.println(
              "[sbt-config] top-level `name` is ignored in multi-module mode (modules derive their name from the module key)"
            )
          cfg
        case Left(error) => throw new MessageOnlyException(s"[sbt-config] $error")
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
         |# Multi-module builds: add a `modules` block (top-level settings become shared).
         |# See https://matejcerny.github.io/sbt-config/ — section "Multi-Module".
         |# Cross-compilation: see section "Cross-Platform Dependencies".
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
         |#
         |# Flat list — all use Scala cross-versioning (%%), included in all projects
         |# dependencies = [
         |#   "org.typelevel:cats-core:2.13.0"
         |# ]
         |#
         |# Language split — scala/java shared everywhere, js/native only in platform projects
         |# dependencies {
         |#   scala  = ["org.typelevel:cats-core:2.13.0"]   # %% (all projects)
         |#   java   = ["com.google.code.gson:gson:2.11.0"] # %  (all projects)
         |#   js     = ["org.scala-js:scalajs-dom:2.8.0"]   # %%% (Scala.js projects only)
         |#   native = ["com.armanbilge:epollcat:0.1.6"]     # %%% (Scala Native projects only)
         |# }
         |#
         |# Full matrix — for cross-compiled projects (JVM + JS/Native)
         |# dependencies {
         |#   shared {
         |#     scala = ["org.typelevel:cats-core:2.13.0"]
         |#   }
         |#   jvm {
         |#     java = ["com.google.code.gson:gson:2.11.0"]
         |#   }
         |#   js     = ["org.scala-js:scalajs-dom:2.8.0"]
         |#   native = ["com.armanbilge:epollcat:0.1.6"]
         |# }
         |
         |# Test dependencies (automatically added with Test scope)
         |# testDependencies = [
         |#   "org.scalatest:scalatest:3.2.19"
         |# ]
         |
         |# Resolvers (additional Maven repositories)
         |# resolvers = [
         |#   { name = "Sonatype Snapshots", url = "https://central.sonatype.com/repository/maven-snapshots/" }
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

  // Detect the platform from the cross-version set by platform plugins.
  // sbt-scalajs sets a prefix starting with "sjs", sbt-scala-native with "native".
  // Both Binary and Full carry a prefix field, so we inspect either.
  private def detectPlatform(cv: CrossVersion): Platform = {
    val prefix = cv match {
      case b: CrossVersion.Binary => b.prefix
      case f: CrossVersion.Full   => f.prefix
      case _                      => ""
    }
    if (prefix.startsWith("sjs")) model.Platform.Js
    else if (prefix.startsWith("native")) model.Platform.Native
    else model.Platform.Jvm
  }

  // Filter dependencies based on detected platform.
  // Shared deps always included. Platform-specific deps only when the project matches.
  private def filterDeps(deps: Seq[Dependency], platform: Platform): Seq[Dependency] =
    deps.filter { dep =>
      dep.platform match {
        case model.Platform.Shared => true
        case p                     => p == platform
      }
    }

  private def toModuleId(dep: Dependency, platformCV: CrossVersion): ModuleID =
    dep.crossVersionType match {
      case CrossVersionType.Java => dep.organization % dep.name % dep.version
      // %%% — platform-suffixed on JS/Native, plain binary on JVM (platformCV adapts per project).
      case CrossVersionType.ScalaJs | CrossVersionType.ScalaNative | CrossVersionType.ScalaPlatform =>
        (dep.organization % dep.name % dep.version).cross(platformCV)
      // %% — standard Scala binary cross-version, never platform-suffixed.
      case CrossVersionType.Scala =>
        dep.organization %% dep.name % dep.version
    }

  private def toResolver(r: model.Resolver): sbt.librarymanagement.MavenRepository =
    sbt.librarymanagement.MavenRepository(r.name, r.url)

  private def toDeveloper(dev: Developer): sbt.Developer =
    sbt.Developer(
      id = dev.id,
      name = dev.name,
      email = dev.email,
      url = url(dev.url)
    )
}
