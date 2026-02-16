package io.github.matejcerny.sbtconfig

import com.typesafe.config.{ Config, ConfigFactory }
import java.io.File
import compat.CollectionConverters._
import scala.io.Source
import scala.util.{ Failure, Success, Try }

/** Parser for HOCON configuration files. Converts Typesafe Config to ProjectConfig model.
  */
object ConfigParser {

  /** Parse a HOCON config file into ProjectConfig.
    *
    * @param file
    *   The config file to parse
    * @return
    *   Either an error message or the parsed ProjectConfig
    */
  def parse(file: File): Either[String, ProjectConfig] =
    if (!file.exists()) {
      Left(s"Config file not found: ${file.getAbsolutePath}")
    } else {
      (
        for {
          source <- Try(Source.fromFile(file))
          content <- Try(source.mkString)
          _ = source.close()
        } yield content
      ) match {
        case Success(content) => parse(content)
        case Failure(e)       => Left(s"Failed to read config file: ${e.getMessage}")
      }
    }

  /** Parse a HOCON config string into ProjectConfig.
    *
    * @param content
    *   The HOCON config string to parse
    * @return
    *   Either an error message or the parsed ProjectConfig
    */
  def parse(content: String): Either[String, ProjectConfig] =
    Try(ConfigFactory.parseString(content).resolve()) match {
      case Success(config) => parseConfig(config)
      case Failure(e)      => Left(s"Failed to parse config: ${e.getMessage}")
    }

  /** Parse a Typesafe Config object into ProjectConfig. Collects all errors instead of failing on the first one.
    */
  private def parseConfig(config: Config): Either[String, ProjectConfig] = {
    val depsResult = parseDependencies(getStringList(config, "dependencies"), "dependencies")
    val testDepsResult = parseDependencies(getStringList(config, "testDependencies"), "testDependencies")
    val developersResult = parseDevelopers(config)

    val errors = Seq(depsResult, testDepsResult, developersResult).collect { case Left(e) => e }

    if (errors.nonEmpty) {
      Left(errors.mkString("; "))
    } else {
      Right(
        ProjectConfig(
          name = getString(config, "name"),
          organization = getString(config, "organization"),
          version = getString(config, "version"),
          scalaVersion = getString(config, "scalaVersion"),
          scalacOptions = getStringList(config, "scalacOptions"),
          dependencies = depsResult.toOption.flatten,
          testDependencies = testDepsResult.toOption.flatten,
          homepage = getString(config, "homepage"),
          licenses = getStringList(config, "licenses"),
          versionScheme = getString(config, "versionScheme"),
          developers = developersResult.toOption.flatten
        )
      )
    }
  }

  /** Parse an optional list of dependency strings into Dependency objects.
    */
  private def parseDependencies(deps: Option[Seq[String]], fieldName: String): Either[String, Option[Seq[Dependency]]] =
    deps match {
      case None => Right(None)
      case Some(depList) =>
        val results = depList.map(parseDependency)
        val errors = results.collect { case Left(e) => e }
        if (errors.nonEmpty) {
          Left(s"Failed to parse $fieldName: ${errors.mkString("; ")}")
        } else {
          Right(Some(results.collect { case Right(d) => d }))
        }
    }

  /** Parse a dependency string in format "group:artifact:version". Assumes cross-versioning by default.
    *
    * @return
    *   Either an error message or the parsed Dependency
    */
  private def parseDependency(input: String): Either[String, Dependency] =
    input.split(":").toList match {
      case org :: name :: version :: Nil =>
        Right(Dependency(org.trim, name.trim, version.trim))
      case _ =>
        Left(s"Invalid dependency format: '$input'. Expected 'organization:name:version'")
    }

  /** Parse developers from config. Each developer is an object with id, name, email, and url.
    */
  private def parseDevelopers(config: Config): Either[String, Option[Seq[Developer]]] =
    if (!config.hasPath("developers")) {
      Right(None)
    } else {
      Try(config.getConfigList("developers").asScala.toSeq) match {
        case Failure(e) => Left(s"Failed to parse developers: ${e.getMessage}")
        case Success(devConfigs) =>
          val results = devConfigs.zipWithIndex.map { case (devConfig, idx) =>
            parseDeveloper(devConfig, idx)
          }
          val errors = results.collect { case Left(e) => e }
          if (errors.nonEmpty) {
            Left(s"Failed to parse developers: ${errors.mkString("; ")}")
          } else {
            Right(Some(results.collect { case Right(d) => d }))
          }
      }
    }

  /** Parse a single developer config object, collecting all missing required fields.
    */
  private def parseDeveloper(devConfig: Config, index: Int): Either[String, Developer] = {
    val requiredFields = Seq("id", "name", "email", "url")
    val missingFields = requiredFields.filterNot(devConfig.hasPath)

    if (missingFields.nonEmpty) {
      Left(s"developer[$index] missing required fields: ${missingFields.mkString(", ")}")
    } else {
      Right(
        Developer(
          id = devConfig.getString("id"),
          name = devConfig.getString("name"),
          email = devConfig.getString("email"),
          url = devConfig.getString("url")
        )
      )
    }
  }

  /** Get value from config if path exists.
    */
  private def getOpt[A](config: Config, path: String)(extract: Config => A): Option[A] =
    if (config.hasPath(path)) Some(extract(config))
    else None

  /** Get an optional string value from config.
    */
  private def getString(config: Config, path: String): Option[String] =
    getOpt(config, path)(_.getString(path))

  /** Get an optional list of strings from config.
    */
  private def getStringList(config: Config, path: String): Option[Seq[String]] =
    getOpt(config, path)(_.getStringList(path).asScala.toSeq)
}
