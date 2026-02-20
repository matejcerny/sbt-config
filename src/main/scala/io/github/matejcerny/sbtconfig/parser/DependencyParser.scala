package io.github.matejcerny.sbtconfig.parser

import com.typesafe.config.{ Config, ConfigValueType }
import io.github.matejcerny.sbtconfig.compat.CollectionConverters._
import io.github.matejcerny.sbtconfig.model.{ CrossVersionType, Dependency, Platform }

/** Parser for dependency fields in HOCON config.
  *
  * Supports three modes:
  *   - Mode 1 (Flat list): all dependencies use `CrossVersionType.Scala` and `Platform.Shared`.
  *   - Mode 2 (Language split): `scala`/`java` map to `Platform.Shared`; `js` to `Platform.Js`; `native` to
  *     `Platform.Native`.
  *   - Mode 3 (Full matrix): explicit platform grouping with `shared`/`jvm` objects and `js`/`native` flat lists.
  */
object DependencyParser {

  private val languageKeys: Map[String, CrossVersionType] = Map(
    "scala" -> CrossVersionType.Scala,
    "java" -> CrossVersionType.Java
  )

  private val mode2Keys: Map[String, (CrossVersionType, Platform)] = Map(
    "scala" -> ((CrossVersionType.Scala, Platform.Shared)),
    "java" -> ((CrossVersionType.Java, Platform.Shared)),
    "js" -> ((CrossVersionType.ScalaJs, Platform.Js)),
    "native" -> ((CrossVersionType.ScalaNative, Platform.Native))
  )

  private val mode3PlatformKeys: Set[String] = Set("shared", "jvm")
  private val mode3FlatKeys: Map[String, (CrossVersionType, Platform)] = Map(
    "js" -> ((CrossVersionType.ScalaJs, Platform.Js)),
    "native" -> ((CrossVersionType.ScalaNative, Platform.Native))
  )
  private val mode3AllKeys: Set[String] = mode3PlatformKeys ++ mode3FlatKeys.keySet

  /** Parse a dependency field that can be a flat list, a language-split object, or a full-matrix object. */
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
          val nested = config.getConfig(fieldName)
          val keys = nested.root().keySet().asScala.toSet
          if (keys.exists(mode3PlatformKeys.contains))
            parseFullMatrix(nested, fieldName)
          else
            parseLanguageSplit(nested, fieldName)
        case other =>
          Left(s"Failed to parse $fieldName: expected a list or object, got ${other.name.toLowerCase}")
      }
    }

  /** Mode 2: Parse a nested dependency object with typed keys (scala, java, js, native). */
  private def parseLanguageSplit(
      config: Config,
      fieldName: String
  ): Either[String, Option[Seq[Dependency]]] = {
    val keys = config.root().keySet().asScala.toSeq
    val unknownKeys = keys.filterNot(mode2Keys.contains)
    if (unknownKeys.nonEmpty) {
      Left(
        s"Failed to parse $fieldName: unknown keys: ${unknownKeys.sorted.mkString(", ")}. " +
          s"Allowed keys: ${mode2Keys.keys.toSeq.sorted.mkString(", ")}"
      )
    } else {
      val results = mode2Keys.toSeq.flatMap { case (key, (cvType, platform)) =>
        if (config.hasPath(key))
          Some(parseDependencyList(config.getStringList(key).asScala.toSeq, s"$fieldName.$key", cvType, platform))
        else
          None
      }
      collectResults(results)
    }
  }

  /** Mode 3: Parse a full-matrix dependency object with shared/jvm/js/native blocks. */
  private def parseFullMatrix(
      config: Config,
      fieldName: String
  ): Either[String, Option[Seq[Dependency]]] = {
    val keys = config.root().keySet().asScala.toSeq
    val unknownKeys = keys.filterNot(mode3AllKeys.contains)
    if (unknownKeys.nonEmpty) {
      Left(
        s"Failed to parse $fieldName: unknown keys: ${unknownKeys.sorted.mkString(", ")}. " +
          s"Allowed keys: ${mode3AllKeys.toSeq.sorted.mkString(", ")}"
      )
    } else {
      val platformResults = mode3PlatformKeys.toSeq.flatMap { key =>
        if (config.hasPath(key)) {
          val platform = if (key == "shared") Platform.Shared else Platform.Jvm
          Some(parsePlatformBlock(config, key, fieldName, platform))
        } else {
          None
        }
      }

      val flatResults = mode3FlatKeys.toSeq.flatMap { case (key, (cvType, platform)) =>
        if (config.hasPath(key))
          Some(parseDependencyList(config.getStringList(key).asScala.toSeq, s"$fieldName.$key", cvType, platform))
        else
          None
      }

      collectResults(platformResults ++ flatResults)
    }
  }

  /** Parse a platform block (shared or jvm) that contains scala/java sub-keys. */
  private def parsePlatformBlock(
      config: Config,
      key: String,
      fieldName: String,
      platform: Platform
  ): Either[String, Option[Seq[Dependency]]] = {
    val blockPath = s"$fieldName.$key"
    config.getValue(key).valueType() match {
      case ConfigValueType.OBJECT =>
        val block = config.getConfig(key)
        val blockKeys = block.root().keySet().asScala.toSeq
        val unknownKeys = blockKeys.filterNot(languageKeys.contains)
        if (unknownKeys.nonEmpty) {
          Left(
            s"Failed to parse $blockPath: unknown keys: ${unknownKeys.sorted.mkString(", ")}. " +
              s"Allowed keys: ${languageKeys.keys.toSeq.sorted.mkString(", ")}"
          )
        } else {
          val results = languageKeys.toSeq.flatMap { case (langKey, cvType) =>
            if (block.hasPath(langKey))
              Some(
                parseDependencyList(block.getStringList(langKey).asScala.toSeq, s"$blockPath.$langKey", cvType, platform)
              )
            else
              None
          }
          collectResults(results)
        }
      case other =>
        Left(s"Failed to parse $blockPath: expected an object, got ${other.name.toLowerCase}")
    }
  }

  /** Parse a list of dependency strings into Dependency objects. */
  private def parseDependencyList(
      deps: Seq[String],
      fieldName: String,
      crossVersionType: CrossVersionType,
      platform: Platform = Platform.Shared
  ): Either[String, Option[Seq[Dependency]]] = {
    val results = deps.map(parseDependency(_, crossVersionType, platform))
    val errors = results.collect { case Left(e) => e }
    if (errors.nonEmpty) {
      Left(s"Failed to parse $fieldName: ${errors.mkString("; ")}")
    } else {
      Right(Some(results.collect { case Right(d) => d }))
    }
  }

  /** Parse a dependency string in format "group:artifact:version". */
  private def parseDependency(
      input: String,
      crossVersionType: CrossVersionType,
      platform: Platform
  ): Either[String, Dependency] =
    input.split(":").toList match {
      case org :: name :: version :: Nil =>
        Right(Dependency(org.trim, name.trim, version.trim, crossVersionType, platform))
      case _ =>
        Left(s"Invalid dependency format: '$input'. Expected 'organization:name:version'")
    }

  /** Collect results from multiple parse operations, accumulating errors. */
  private def collectResults(
      results: Seq[Either[String, Option[Seq[Dependency]]]]
  ): Either[String, Option[Seq[Dependency]]] = {
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
