package io.github.matejcerny.sbtconfig.model

/** Cross-versioning type for dependencies. Determines how the dependency artifact name is resolved.
  *
  *   - `Java` → `%` (no cross-versioning, plain Java dependency)
  *   - `Scala` → `.cross(platformCV)`: plain `%%` on JVM, platform-suffixed (`%%%`) on JS/Native. `platformCV` adapts
  *     per project — `CrossVersion.binary` by default, overridden to the platform suffix when sbt-scalajs /
  *     sbt-scala-native is active — so a Scala dependency links on whichever platform the project targets.
  */
sealed abstract class CrossVersionType
object CrossVersionType {
  case object Java extends CrossVersionType
  case object Scala extends CrossVersionType
}
