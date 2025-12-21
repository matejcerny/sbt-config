sys.props.get("plugin.version") match {
  case Some(v) => addSbtPlugin("cz.matejcerny" % "sbt-config" % v)
  case _ => sys.error("The system property 'plugin.version' is not defined.")
}
