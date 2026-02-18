package io.github.matejcerny.sbtconfig.parser

import com.typesafe.config.Config
import io.github.matejcerny.sbtconfig.compat.CollectionConverters._
import io.github.matejcerny.sbtconfig.model.Developer
import scala.util.{ Failure, Success, Try }

/** Parser for developer fields in HOCON config. */
object DeveloperParser {

  /** Parse developers from config. Each developer is an object with id, name, email, and url. */
  def parseDevelopers(config: Config): Either[String, Option[Seq[Developer]]] =
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

  /** Parse a single developer config object, collecting all missing required fields. */
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
}
