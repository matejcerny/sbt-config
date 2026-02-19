package io.github.matejcerny.sbtconfig

object compat {
  val CollectionConverters = scala.jdk.CollectionConverters

  type LicenseResult = sbt.librarymanagement.License
}
