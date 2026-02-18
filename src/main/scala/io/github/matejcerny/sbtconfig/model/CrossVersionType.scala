package io.github.matejcerny.sbtconfig.model

/** Cross-versioning type for dependencies. Determines how the dependency artifact name is resolved.
  *
  *   - `Scala` → `%%` (standard Scala cross-versioning)
  *   - `Java` → `%` (no cross-versioning, plain Java dependency)
  *   - `ScalaJs` → `%%%` (platform cross-version, requires sbt-scalajs plugin)
  *   - `ScalaNative` → `%%%` (platform cross-version, requires sbt-scala-native plugin)
  */
sealed abstract class CrossVersionType
object CrossVersionType {
  case object Scala extends CrossVersionType
  case object Java extends CrossVersionType
  case object ScalaJs extends CrossVersionType
  case object ScalaNative extends CrossVersionType
}
