package io.github.matejcerny.sbtconfig.model

/** Platform target for dependencies. Determines which sbt projects receive the dependency.
  *
  *   - `Shared` → all platforms (JVM, JS, Native)
  *   - `Jvm` → JVM projects only
  *   - `Js` → Scala.js projects only
  *   - `Native` → Scala Native projects only
  */
sealed abstract class Platform
object Platform {
  case object Shared extends Platform
  case object Jvm extends Platform
  case object Js extends Platform
  case object Native extends Platform
}
