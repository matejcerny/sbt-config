package io.github.matejcerny.sbtconfig

import io.github.matejcerny.sbtconfig.ProjectConfig.Example
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

class ConfigParserSpec extends AnyFlatSpec with Matchers with EitherValues {

  "ConfigParser.parse(String)" should "parse a complete config" in {
    val config =
      """
        |name = "my-project"
        |organization = "com.example"
        |version = "1.0.0"
        |scalaVersion = "3.3.4"
        |scalacOptions = ["-deprecation", "-feature"]
        |dependencies = [
        |  "org.typelevel:cats-core:2.13.0",
        |  "io.circe:circe-core:0.14.10"
        |]
        |testDependencies = [
        |  "org.scalatest:scalatest:3.2.19"
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val projectConfig = result.value
    projectConfig.name shouldBe Some("my-project")
    projectConfig.organization shouldBe Some("com.example")
    projectConfig.version shouldBe Some("1.0.0")
    projectConfig.scalaVersion shouldBe Some("3.3.4")
    projectConfig.scalacOptions shouldBe Some(Seq("-deprecation", "-feature"))
    projectConfig.dependencies shouldBe Some(
      Seq(
        Dependency("org.typelevel", "cats-core", "2.13.0"),
        Dependency("io.circe", "circe-core", "0.14.10")
      )
    )
    projectConfig.testDependencies shouldBe Some(
      Seq(
        Dependency("org.scalatest", "scalatest", "3.2.19")
      )
    )
  }

  it should "parse a minimal config with only some fields" in {
    val config =
      """
        |name = "minimal-project"
        |scalaVersion = "3.3.4"
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val projectConfig = result.value
    projectConfig.name shouldBe Some("minimal-project")
    projectConfig.organization shouldBe None
    projectConfig.version shouldBe None
    projectConfig.scalaVersion shouldBe Some("3.3.4")
    projectConfig.scalacOptions shouldBe None
    projectConfig.dependencies shouldBe None
    projectConfig.testDependencies shouldBe None
  }

  it should "parse an empty config" in {
    val result = ConfigParser.parse("")

    result.isRight shouldBe true
    result.value shouldBe ProjectConfig.empty
  }

  it should "return error for invalid HOCON syntax" in {
    val invalidConfig =
      """
        |name = "unclosed string
        |""".stripMargin

    val result = ConfigParser.parse(invalidConfig)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse config")
  }

  it should "return error for invalid dependency format" in {
    val config =
      """
        |dependencies = ["invalid-dependency"]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies")
    result.left.value should include("Invalid dependency format")
  }

  it should "return error for dependency with too many parts" in {
    val config =
      """
        |dependencies = ["org:name:version:extra"]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Invalid dependency format")
  }

  it should "collect all dependency parsing errors within a field" in {
    val config =
      """
        |dependencies = ["invalid1", "invalid2"]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("invalid1")
    result.left.value should include("invalid2")
  }

  it should "collect errors from both dependencies and testDependencies" in {
    val config =
      """
        |dependencies = ["invalid-dep"]
        |testDependencies = ["invalid-test-dep"]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("dependencies")
    result.left.value should include("invalid-dep")
    result.left.value should include("testDependencies")
    result.left.value should include("invalid-test-dep")
  }

  it should "trim whitespace from dependency parts" in {
    val config =
      """
        |dependencies = [" org.typelevel : cats-core : 2.13.0 "]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    result.value.dependencies shouldBe Some(
      Seq(
        Dependency("org.typelevel", "cats-core", "2.13.0")
      )
    )
  }

  it should "handle HOCON substitutions" in {
    @scala.annotation.nowarn("msg=possible missing interpolator")
    val config =
      """
        |base-version = "1.0"
        |version = ${base-version}".0-SNAPSHOT"
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    result.value.version shouldBe Some("1.0.0-SNAPSHOT")
  }

  it should "parse config matching Example values" in {
    def depToString(d: Dependency): String = s""""${d.organization}:${d.name}:${d.version}""""
    def devToString(d: Developer): String =
      s"""{ id = "${d.id}", name = "${d.name}", email = "${d.email}", url = "${d.url}" }"""
    val config =
      s"""
        |name = "${Example.name}"
        |organization = "${Example.organization}"
        |version = "${Example.version}"
        |scalaVersion = "${Example.scalaVersion}"
        |scalacOptions = ${Example.scalacOptions.map(s => s""""$s"""").mkString("[", ", ", "]")}
        |dependencies = ${Example.dependencies.map(depToString).mkString("[", ", ", "]")}
        |testDependencies = ${Example.testDependencies.map(depToString).mkString("[", ", ", "]")}
        |homepage = "${Example.homepage}"
        |licenses = ${Example.licenses.map(s => s""""$s"""").mkString("[", ", ", "]")}
        |versionScheme = "${Example.versionScheme}"
        |developers = ${Example.developers.map(devToString).mkString("[", ", ", "]")}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val projectConfig = result.value
    projectConfig.name shouldBe Some(Example.name)
    projectConfig.organization shouldBe Some(Example.organization)
    projectConfig.version shouldBe Some(Example.version)
    projectConfig.scalaVersion shouldBe Some(Example.scalaVersion)
    projectConfig.scalacOptions shouldBe Some(Example.scalacOptions)
    projectConfig.dependencies shouldBe Some(Example.dependencies)
    projectConfig.testDependencies shouldBe Some(Example.testDependencies)
    projectConfig.homepage shouldBe Some(Example.homepage)
    projectConfig.licenses shouldBe Some(Example.licenses)
    projectConfig.versionScheme shouldBe Some(Example.versionScheme)
    projectConfig.developers shouldBe Some(Example.developers)
  }

  it should "parse publishing settings" in {
    val config =
      """
        |homepage = "https://github.com/example/project"
        |licenses = ["MIT", "Apache2"]
        |versionScheme = "early-semver"
        |developers = [
        |  { id = "dev1", name = "Developer One", email = "dev1@example.com", url = "https://dev1.example.com" },
        |  { id = "dev2", name = "Developer Two", email = "dev2@example.com", url = "https://dev2.example.com" }
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val projectConfig = result.value
    projectConfig.homepage shouldBe Some("https://github.com/example/project")
    projectConfig.licenses shouldBe Some(Seq("MIT", "Apache2"))
    projectConfig.versionScheme shouldBe Some("early-semver")
    projectConfig.developers shouldBe Some(
      Seq(
        Developer("dev1", "Developer One", "dev1@example.com", "https://dev1.example.com"),
        Developer("dev2", "Developer Two", "dev2@example.com", "https://dev2.example.com")
      )
    )
  }

  it should "return error for invalid developer format with all missing fields" in {
    val config =
      """
        |developers = [
        |  { id = "dev1", name = "Developer One" }
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse developers")
    result.left.value should include("email")
    result.left.value should include("url")
  }

  it should "collect errors from multiple invalid developers" in {
    val config =
      """
        |developers = [
        |  { id = "dev1" },
        |  { name = "Developer Two" }
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("developer[0]")
    result.left.value should include("developer[1]")
    result.left.value should include("name")
    result.left.value should include("email")
    result.left.value should include("url")
    result.left.value should include("id")
  }

  "License.supported" should "contain sbt-supported licenses" in {
    License.supported should contain("MIT")
    License.supported should contain("Apache2")
    License.supported should contain("GPL3")
    License.supported should contain("CC0")
    License.supported should have size 4
  }

  "ConfigParser.parse(File)" should "parse a valid config file" in {
    val tempFile = Files.createTempFile("test-config", ".conf").toFile
    tempFile.deleteOnExit()
    Files.writeString(
      tempFile.toPath,
      s"""
        |name = "${Example.name}"
        |scalaVersion = "${Example.scalaVersion}"
        |""".stripMargin
    )

    val result = ConfigParser.parse(tempFile)

    result.isRight shouldBe true
    result.value.name shouldBe Some(Example.name)
    result.value.scalaVersion shouldBe Some(Example.scalaVersion)
  }

  it should "return error for non-existent file" in {
    val file = new File("/non/existent/path/config.conf")
    val result = ConfigParser.parse(file)

    result.isLeft shouldBe true
    result.left.value should include("Config file not found")
  }

  it should "return error when file cannot be read" in {
    val tempDir = Files.createTempDirectory("test-config-dir")
    tempDir.toFile.deleteOnExit()

    val result = ConfigParser.parse(tempDir.toFile)

    result.isLeft shouldBe true
    result.left.value should include("Failed to read config file")
  }
}
