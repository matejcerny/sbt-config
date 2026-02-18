package io.github.matejcerny.sbtconfig

import java.net.URL

object compat {
  val CollectionConverters = scala.collection.JavaConverters

  type LicenseResult = (String, URL)
}
