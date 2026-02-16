package io.github.matejcerny.sbtconfig

import java.net.URL

object compat {
  val CollectionConverters = scala.collection.JavaConverters

  type LicenseResult = (String, URL)

  def toLicense(licenseId: String): Option[LicenseResult] = licenseId match {
    case "Apache2" => Some(sbt.librarymanagement.License.Apache2)
    case "MIT"     => Some(sbt.librarymanagement.License.MIT)
    case "CC0"     => Some(sbt.librarymanagement.License.CC0)
    case "GPL3"    => Some(sbt.librarymanagement.License.GPL3_or_later)
    case _ =>
      System.err.println(
        s"[sbt-config] Unknown license: '$licenseId'. Supported: ${License.supported.mkString(", ")}"
      )
      None
  }
}
