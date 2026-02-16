package io.github.matejcerny.sbtconfig

object compat {
  val CollectionConverters = scala.jdk.CollectionConverters

  type LicenseResult = sbt.librarymanagement.License

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
