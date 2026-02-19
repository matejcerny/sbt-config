package io.github.matejcerny.sbtconfig.model

/** Represents a project developer.
  *
  * @param id
  *   Developer ID (e.g., GitHub username)
  * @param name
  *   Full name
  * @param email
  *   Email address
  * @param url
  *   Personal/professional URL
  */
case class Developer(
    id: String,
    name: String,
    email: String,
    url: String
)
