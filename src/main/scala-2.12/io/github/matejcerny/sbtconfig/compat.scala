package io.github.matejcerny.sbtconfig

import java.net.URL

object compat {
  def projectUrl(value: String): URL = sbt.url(value)

  val CollectionConverters = scala.collection.JavaConverters

  type LicenseResult = (String, URL)
}
