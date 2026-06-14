sys.props.get("plugin.version") match {
  case Some(v) => addSbtPlugin("io.github.matejcerny" % "sbt-config" % v)
  case _ => sys.error("The system property 'plugin.version' is not defined.")
}

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.10")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
