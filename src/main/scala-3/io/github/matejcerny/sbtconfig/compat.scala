package io.github.matejcerny.sbtconfig

object compat {
  def projectUrl(value: String): java.net.URI = sbt.uri(value)

  val CollectionConverters = scala.jdk.CollectionConverters

  type LicenseResult = sbt.librarymanagement.License
}
