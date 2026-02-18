package io.github.matejcerny.sbtconfig.model

import io.github.matejcerny.sbtconfig.compat

/** Supported license identifiers matching sbt.librarymanagement.License */
object License {
  val supported: Seq[String] = Seq("Apache2", "MIT", "CC0", "GPL3")

  def toLicense(licenseId: String): Option[compat.LicenseResult] = licenseId match {
    case "Apache2" => Some(sbt.librarymanagement.License.Apache2)
    case "MIT"     => Some(sbt.librarymanagement.License.MIT)
    case "CC0"     => Some(sbt.librarymanagement.License.CC0)
    case "GPL3"    => Some(sbt.librarymanagement.License.GPL3_or_later)
    case _ =>
      System.err.println(
        s"[sbt-config] Unknown license: '$licenseId'. Supported: ${supported.mkString(", ")}"
      )
      None
  }
}
