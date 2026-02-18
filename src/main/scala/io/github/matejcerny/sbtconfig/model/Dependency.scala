package io.github.matejcerny.sbtconfig.model

/** Represents a single dependency.
  *
  * @param organization
  *   Group/organization ID (e.g., "org.typelevel")
  * @param name
  *   Artifact name (e.g., "cats-core")
  * @param version
  *   Version string (e.g., "2.13.0")
  * @param crossVersionType
  *   How the artifact name is cross-versioned (default: Scala `%%`)
  */
case class Dependency(
    organization: String,
    name: String,
    version: String,
    crossVersionType: CrossVersionType = CrossVersionType.Scala
)
