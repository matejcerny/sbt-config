package io.github.matejcerny.sbtconfig.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try

class LicenseSpec extends AnyFlatSpec with Matchers {

  "License.supported" should "contain sbt-supported licenses" in {
    License.supported should contain("MIT")
    License.supported should contain("Apache2")
    License.supported should contain("GPL3")
    License.supported should contain("CC0")
    License.supported should have size 4
  }

  "License.toLicense" should "convert all supported license identifiers" in {
    License.toLicense("MIT") shouldBe defined
    License.toLicense("Apache2") shouldBe defined
    License.toLicense("GPL3") shouldBe defined
    License.toLicense("CC0") shouldBe defined
  }

  it should "return None for unknown license identifiers" in {
    val errStream = new java.io.ByteArrayOutputStream()
    val oldErr = System.err
    System.setErr(new java.io.PrintStream(errStream))
    val result = Try {
      License.toLicense("Unknown-License") shouldBe None
      errStream.toString should include("Unknown license: 'Unknown-License'")
    }
    System.setErr(oldErr)
    result.get
  }
}
