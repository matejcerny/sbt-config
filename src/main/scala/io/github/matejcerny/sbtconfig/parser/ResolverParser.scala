package io.github.matejcerny.sbtconfig.parser

import com.typesafe.config.Config
import io.github.matejcerny.sbtconfig.compat.CollectionConverters._
import io.github.matejcerny.sbtconfig.model.Resolver
import scala.util.{ Failure, Success, Try }

/** Parser for resolver fields in HOCON config. */
object ResolverParser {

  /** Parse resolvers from config. Each resolver is an object with name and url. */
  def parseResolvers(config: Config): Either[String, Option[Seq[Resolver]]] =
    if (!config.hasPath("resolvers")) {
      Right(None)
    } else {
      Try(config.getConfigList("resolvers").asScala.toSeq) match {
        case Failure(e) => Left(s"Failed to parse resolvers: ${e.getMessage}")
        case Success(resolverConfigs) =>
          val results = resolverConfigs.zipWithIndex.map { case (resolverConfig, idx) =>
            parseResolver(resolverConfig, idx)
          }
          val errors = results.collect { case Left(e) => e }
          if (errors.nonEmpty) {
            Left(s"Failed to parse resolvers: ${errors.mkString("; ")}")
          } else {
            Right(Some(results.collect { case Right(r) => r }))
          }
      }
    }

  /** Parse a single resolver config object, collecting all missing required fields. */
  private def parseResolver(resolverConfig: Config, index: Int): Either[String, Resolver] = {
    val requiredFields = Seq("name", "url")
    val missingFields = requiredFields.filterNot(resolverConfig.hasPath)

    if (missingFields.nonEmpty) {
      Left(s"resolver[$index] missing required fields: ${missingFields.mkString(", ")}")
    } else {
      Right(
        Resolver(
          name = resolverConfig.getString("name"),
          url = resolverConfig.getString("url")
        )
      )
    }
  }
}
