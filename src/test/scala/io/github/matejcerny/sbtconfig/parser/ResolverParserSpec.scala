package io.github.matejcerny.sbtconfig.parser

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ResolverParserSpec extends AnyFlatSpec with Matchers with EitherValues {

  "ResolverParser" should "return error for missing url field" in {
    val config =
      """
        |resolvers = [
        |  { name = "Sonatype Snapshots" }
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse resolvers")
    result.left.value should include("url")
  }

  it should "collect errors from multiple invalid resolvers" in {
    val config =
      """
        |resolvers = [
        |  { name = "Snapshots" },
        |  { url = "https://example.com" }
        |]
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("resolver[0]")
    result.left.value should include("resolver[1]")
    result.left.value should include("url")
    result.left.value should include("name")
  }

  it should "return error when resolvers is not a list of objects" in {
    val config =
      """
        |resolvers = "not a list"
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse resolvers")
  }
}
