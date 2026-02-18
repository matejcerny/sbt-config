package io.github.matejcerny.sbtconfig.parser

import com.typesafe.config.{ Config, ConfigValueType }
import io.github.matejcerny.sbtconfig.compat.CollectionConverters._
import io.github.matejcerny.sbtconfig.model.{ CrossVersionType, Dependency }

/** Parser for dependency fields in HOCON config. */
object DependencyParser {

  private val dependencyKeys: Map[String, CrossVersionType] = Map(
    "scala" -> CrossVersionType.Scala,
    "java" -> CrossVersionType.Java,
    "js" -> CrossVersionType.ScalaJs,
    "native" -> CrossVersionType.ScalaNative
  )

  /** Parse a dependency field that can be either a flat list (backwards compatible) or a nested object with typed keys.
    *
    * Flat list: `dependencies = ["org:name:version"]` → all dependencies use `CrossVersionType.Scala`
    *
    * Nested object: `dependencies { scala = [...], java = [...], js = [...], native = [...] }` → each key maps to its
    * `CrossVersionType`
    */
  def parseDependencyField(
      config: Config,
      fieldName: String
  ): Either[String, Option[Seq[Dependency]]] =
    if (!config.hasPath(fieldName)) {
      Right(None)
    } else {
      config.getValue(fieldName).valueType() match {
        case ConfigValueType.LIST =>
          parseDependencyList(config.getStringList(fieldName).asScala.toSeq, fieldName, CrossVersionType.Scala)
        case ConfigValueType.OBJECT =>
          parseNestedDependencies(config.getConfig(fieldName), fieldName)
        case other =>
          Left(s"Failed to parse $fieldName: expected a list or object, got ${other.name.toLowerCase}")
      }
    }

  /** Parse a nested dependency object with typed keys (scala, java, js, native). */
  private def parseNestedDependencies(
      config: Config,
      fieldName: String
  ): Either[String, Option[Seq[Dependency]]] = {
    val keys = config.root().keySet().asScala.toSeq
    val unknownKeys = keys.filterNot(dependencyKeys.contains)
    if (unknownKeys.nonEmpty) {
      Left(
        s"Failed to parse $fieldName: unknown keys: ${unknownKeys.sorted.mkString(", ")}. " +
          s"Allowed keys: ${dependencyKeys.keys.toSeq.sorted.mkString(", ")}"
      )
    } else {
      val results = dependencyKeys.toSeq.flatMap { case (key, cvType) =>
        if (config.hasPath(key))
          Some(parseDependencyList(config.getStringList(key).asScala.toSeq, s"$fieldName.$key", cvType))
        else
          None
      }
      val errors = results.collect { case Left(e) => e }
      if (errors.nonEmpty) {
        Left(errors.mkString("; "))
      } else {
        val deps = results.flatMap(_.toOption).flatten.flatten
        if (deps.isEmpty) Right(None)
        else Right(Some(deps))
      }
    }
  }

  /** Parse a list of dependency strings into Dependency objects with the given CrossVersionType. */
  private def parseDependencyList(
      deps: Seq[String],
      fieldName: String,
      crossVersionType: CrossVersionType
  ): Either[String, Option[Seq[Dependency]]] = {
    val results = deps.map(parseDependency(_, crossVersionType))
    val errors = results.collect { case Left(e) => e }
    if (errors.nonEmpty) {
      Left(s"Failed to parse $fieldName: ${errors.mkString("; ")}")
    } else {
      Right(Some(results.collect { case Right(d) => d }))
    }
  }

  /** Parse a dependency string in format "group:artifact:version". */
  private def parseDependency(input: String, crossVersionType: CrossVersionType): Either[String, Dependency] =
    input.split(":").toList match {
      case org :: name :: version :: Nil =>
        Right(Dependency(org.trim, name.trim, version.trim, crossVersionType))
      case _ =>
        Left(s"Invalid dependency format: '$input'. Expected 'organization:name:version'")
    }
}
