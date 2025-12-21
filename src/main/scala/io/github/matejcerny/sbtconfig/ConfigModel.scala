package io.github.matejcerny.sbtconfig

/** Represents a single dependency.
  *
  * @param organization
  *   Group/organization ID (e.g., "org.typelevel")
  * @param name
  *   Artifact name (e.g., "cats-core")
  * @param version
  *   Version string (e.g., "2.13.0")
  * @param crossVersion
  *   Whether to use Scala cross-versioning (%% in sbt)
  */
case class Dependency(
    organization: String,
    name: String,
    version: String,
    crossVersion: Boolean = true
)

/** Configuration model representing the HOCON config structure. All fields are optional to allow partial configuration.
  *
  * @param name
  *   Project name
  * @param organization
  *   Organization/group ID
  * @param version
  *   Project version
  * @param scalaVersion
  *   Scala compiler version
  * @param scalacOptions
  *   Scala compiler options
  * @param dependencies
  *   Compile dependencies
  * @param testDependencies
  *   Test dependencies
  */
case class ProjectConfig(
    name: Option[String] = None,
    organization: Option[String] = None,
    version: Option[String] = None,
    scalaVersion: Option[String] = None,
    scalacOptions: Option[Seq[String]] = None,
    dependencies: Option[Seq[Dependency]] = None,
    testDependencies: Option[Seq[Dependency]] = None
)

object ProjectConfig {

  /** Empty configuration with all defaults */
  val empty: ProjectConfig = ProjectConfig()

  /** Example values for documentation and default config generation */
  object Example {
    val name = "example-project"
    val organization = "com.example"
    val version = "0.1.0-SNAPSHOT"
    val scalaVersion = "3.3.4"
    val scalacOptions: Seq[String] = Seq("-deprecation", "-feature", "-unchecked")
    val dependencies: Seq[Dependency] = Seq(
      Dependency("org.typelevel", "cats-core", "2.13.0")
    )
    val testDependencies: Seq[Dependency] = Seq(
      Dependency("org.scalatest", "scalatest", "3.2.19")
    )
  }
}
