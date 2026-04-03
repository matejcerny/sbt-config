package io.github.matejcerny.sbtconfig.model

/** Represents a Maven repository resolver.
  *
  * @param name
  *   Repository name
  * @param url
  *   Repository URL
  */
case class Resolver(
    name: String,
    url: String
)
