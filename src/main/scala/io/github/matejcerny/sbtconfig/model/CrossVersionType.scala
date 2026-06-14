package io.github.matejcerny.sbtconfig.model

/** Cross-versioning type for dependencies. Determines how the dependency artifact name is resolved.
  *
  *   - `Scala` → `%%` (standard Scala binary cross-versioning, never platform-suffixed)
  *   - `Java` → `%` (no cross-versioning, plain Java dependency)
  *   - `ScalaJs` → `%%%` (platform cross-version, requires sbt-scalajs plugin)
  *   - `ScalaNative` → `%%%` (platform cross-version, requires sbt-scala-native plugin)
  *   - `ScalaPlatform` → `%%%` (platform-adaptive: plain `%%` on JVM, platform-suffixed on JS/Native). Used for the
  *     full-matrix `shared` block so a shared Scala dependency links on every targeted platform.
  */
sealed abstract class CrossVersionType
object CrossVersionType {
  case object Scala extends CrossVersionType
  case object Java extends CrossVersionType
  case object ScalaJs extends CrossVersionType
  case object ScalaNative extends CrossVersionType
  case object ScalaPlatform extends CrossVersionType
}
