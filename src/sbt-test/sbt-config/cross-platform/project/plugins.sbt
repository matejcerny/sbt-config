sys.props.get("plugin.version") match {
  case Some(v) => addSbtPlugin("io.github.matejcerny" % "sbt-config" % v)
  case _ => sys.error("The system property 'plugin.version' is not defined.")
}

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.2")
