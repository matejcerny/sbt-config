package io.github.matejcerny.sbtconfig.parser

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeveloperParserSpec extends AnyFlatSpec with Matchers with EitherValues {

  "DeveloperParser" should "return error for invalid developer format with all missing fields" in {
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

  it should "return error when developers is not a list of objects" in {
    val config =
      """
        |developers = "not a list"
        |""".stripMargin

    val result = ConfigParser.parse(config)

    result.isLeft shouldBe true
    result.left.value should include("Failed to parse developers")
  }
}
