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

/** Represents a project developer.
  *
  * @param id
  *   Developer ID (e.g., GitHub username)
  * @param name
  *   Full name
  * @param email
  *   Email address
  * @param url
  *   Personal/professional URL
  */
case class Developer(
    id: String,
    name: String,
    email: String,
    url: String
)

/** Supported license identifiers matching sbt.librarymanagement.License */
object License {
  val supported: Seq[String] = Seq("Apache2", "MIT", "CC0", "GPL3")
}

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
  * @param homepage
  *   Project homepage URL
  * @param licenses
  *   List of license identifiers (e.g., "MIT", "Apache-2.0")
  * @param versionScheme
  *   Version scheme (e.g., "early-semver", "semver-spec")
  * @param developers
  *   List of project developers
  */
case class ProjectConfig(
    name: Option[String] = None,
    organization: Option[String] = None,
    version: Option[String] = None,
    scalaVersion: Option[String] = None,
    scalacOptions: Option[Seq[String]] = None,
    dependencies: Option[Seq[Dependency]] = None,
    testDependencies: Option[Seq[Dependency]] = None,
    homepage: Option[String] = None,
    licenses: Option[Seq[String]] = None,
    versionScheme: Option[String] = None,
    developers: Option[Seq[Developer]] = None
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
    val homepage = "https://github.com/example/example-project"
    val licenses: Seq[String] = Seq("MIT")
    val versionScheme = "early-semver"
    val developers: Seq[Developer] = Seq(
      Developer("johndoe", "John Doe", "john@example.com", "https://example.com")
    )
  }
}
