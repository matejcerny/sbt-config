package io.github.matejcerny.sbtconfig.parser

import io.github.matejcerny.sbtconfig.model._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DependencyParserSpec extends AnyFlatSpec with Matchers with EitherValues {

  "DependencyParser" should "return error for invalid dependency format" in {
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

  // Mode 1 — Flat list

  it should "parse flat list with Platform.Shared" in {
    val config =
      """
        |dependencies = ["org.typelevel:cats-core:2.13.0"]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 1
    deps.head shouldBe Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala, Platform.Shared)
  }

  // Mode 2 — Language split

  it should "parse nested dependencies with all four keys and correct platforms" in {
    val config =
      """
        |dependencies {
        |  scala  = ["org.typelevel:cats-core:2.13.0"]
        |  java   = ["com.google.code.gson:gson:2.11.0"]
        |  js     = ["org.scala-js:scalajs-dom:2.8.0"]
        |  native = ["com.armanbilge:epollcat:0.1.6"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 4
    deps should contain(Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("com.google.code.gson", "gson", "2.11.0", CrossVersionType.Java, Platform.Shared))
    deps should contain(Dependency("org.scala-js", "scalajs-dom", "2.8.0", CrossVersionType.ScalaJs, Platform.Js))
    deps should contain(
      Dependency("com.armanbilge", "epollcat", "0.1.6", CrossVersionType.ScalaNative, Platform.Native)
    )
  }

  it should "parse nested dependencies with only some keys" in {
    val config =
      """
        |dependencies {
        |  scala = ["org.typelevel:cats-core:2.13.0"]
        |  java  = ["com.google.code.gson:gson:2.11.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 2
    deps should contain(Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("com.google.code.gson", "gson", "2.11.0", CrossVersionType.Java, Platform.Shared))
  }

  it should "parse nested testDependencies" in {
    val config =
      """
        |testDependencies {
        |  scala = ["org.scalatest:scalatest:3.2.19"]
        |  java  = ["junit:junit:4.13.2"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.testDependencies.get
    deps should have size 2
    deps should contain(Dependency("org.scalatest", "scalatest", "3.2.19", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("junit", "junit", "4.13.2", CrossVersionType.Java, Platform.Shared))
  }

  it should "support flat dependencies with nested testDependencies" in {
    val config =
      """
        |dependencies = ["org.typelevel:cats-core:2.13.0"]
        |testDependencies {
        |  scala = ["org.scalatest:scalatest:3.2.19"]
        |  java  = ["junit:junit:4.13.2"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    result.value.dependencies shouldBe Some(
      Seq(Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala))
    )
    val testDeps = result.value.testDependencies.get
    testDeps should have size 2
    testDeps should contain(Dependency("org.scalatest", "scalatest", "3.2.19", CrossVersionType.Scala, Platform.Shared))
    testDeps should contain(Dependency("junit", "junit", "4.13.2", CrossVersionType.Java, Platform.Shared))
  }

  it should "return None for empty nested dependencies object" in {
    val config =
      """
        |dependencies {}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    result.value.dependencies shouldBe None
  }

  it should "return error for unknown keys in nested dependencies" in {
    val config =
      """
        |dependencies {
        |  scala   = ["org.typelevel:cats-core:2.13.0"]
        |  unknown = ["bad:dep:1.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("unknown keys: unknown")
    result.left.value should include("Allowed keys")
  }

  it should "return error for invalid dependency within nested object" in {
    val config =
      """
        |dependencies {
        |  java = ["invalid-java-dep"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies.java")
    result.left.value should include("Invalid dependency format")
  }

  it should "return error when dependencies is a wrong value type" in {
    val config =
      """
        |dependencies = "not a list or object"
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies")
    result.left.value should include("expected a list or object")
  }

  // Mode 3 — Full matrix

  it should "parse full matrix with shared, jvm, js, and native" in {
    val config =
      """
        |dependencies {
        |  shared {
        |    scala = ["org.typelevel:cats-core:2.13.0"]
        |    java  = ["org.slf4j:slf4j-api:2.0.16"]
        |  }
        |  jvm {
        |    scala = ["org.typelevel:cats-effect:3.5.0"]
        |    java  = ["com.google.code.gson:gson:2.11.0"]
        |  }
        |  js     = ["org.scala-js:scalajs-dom:2.8.0"]
        |  native = ["com.armanbilge:epollcat:0.1.6"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 6
    deps should contain(Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("org.slf4j", "slf4j-api", "2.0.16", CrossVersionType.Java, Platform.Shared))
    deps should contain(Dependency("org.typelevel", "cats-effect", "3.5.0", CrossVersionType.Scala, Platform.Jvm))
    deps should contain(Dependency("com.google.code.gson", "gson", "2.11.0", CrossVersionType.Java, Platform.Jvm))
    deps should contain(Dependency("org.scala-js", "scalajs-dom", "2.8.0", CrossVersionType.ScalaJs, Platform.Js))
    deps should contain(
      Dependency("com.armanbilge", "epollcat", "0.1.6", CrossVersionType.ScalaNative, Platform.Native)
    )
  }

  it should "parse full matrix with only shared and js" in {
    val config =
      """
        |dependencies {
        |  shared {
        |    scala = ["org.typelevel:cats-core:2.13.0"]
        |  }
        |  js = ["org.scala-js:scalajs-dom:2.8.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 2
    deps should contain(Dependency("org.typelevel", "cats-core", "2.13.0", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("org.scala-js", "scalajs-dom", "2.8.0", CrossVersionType.ScalaJs, Platform.Js))
  }

  it should "parse full matrix with only jvm block" in {
    val config =
      """
        |dependencies {
        |  jvm {
        |    java = ["com.google.code.gson:gson:2.11.0"]
        |  }
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.dependencies.get
    deps should have size 1
    deps.head shouldBe Dependency("com.google.code.gson", "gson", "2.11.0", CrossVersionType.Java, Platform.Jvm)
  }

  it should "return None for full matrix with empty blocks" in {
    val config =
      """
        |dependencies {
        |  shared {
        |  }
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    result.value.dependencies shouldBe None
  }

  it should "return error for unknown keys in full matrix" in {
    val config =
      """
        |dependencies {
        |  shared {
        |    scala = ["org.typelevel:cats-core:2.13.0"]
        |  }
        |  unknown = ["bad:dep:1.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("unknown keys: unknown")
    result.left.value should include("Allowed keys")
  }

  it should "return error for unknown keys inside shared block" in {
    val config =
      """
        |dependencies {
        |  shared {
        |    scala   = ["org.typelevel:cats-core:2.13.0"]
        |    unknown = ["bad:dep:1.0"]
        |  }
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies.shared")
    result.left.value should include("unknown keys: unknown")
  }

  it should "return error when shared is not an object" in {
    val config =
      """
        |dependencies {
        |  shared = ["org.typelevel:cats-core:2.13.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies.shared")
    result.left.value should include("expected an object")
  }

  it should "return error when jvm is not an object" in {
    val config =
      """
        |dependencies {
        |  jvm = ["org.typelevel:cats-core:2.13.0"]
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies.jvm")
    result.left.value should include("expected an object")
  }

  it should "return error for invalid dependency inside full matrix block" in {
    val config =
      """
        |dependencies {
        |  shared {
        |    scala = ["invalid-dep"]
        |  }
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse dependencies.shared.scala")
    result.left.value should include("Invalid dependency format")
  }

  it should "parse full matrix testDependencies" in {
    val config =
      """
        |testDependencies {
        |  shared {
        |    scala = ["org.scalatest:scalatest:3.2.19"]
        |  }
        |  jvm {
        |    java = ["junit:junit:4.13.2"]
        |  }
        |}
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isRight shouldBe true
    val deps = result.value.testDependencies.get
    deps should have size 2
    deps should contain(Dependency("org.scalatest", "scalatest", "3.2.19", CrossVersionType.Scala, Platform.Shared))
    deps should contain(Dependency("junit", "junit", "4.13.2", CrossVersionType.Java, Platform.Jvm))
  }
}
