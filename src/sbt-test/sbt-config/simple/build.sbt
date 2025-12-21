// Assertions - these tasks fail if values don't match build.conf
val checkName = taskKey[Unit]("Check name")
checkName := {
  assert(name.value == "test-project", s"Expected 'test-project', got '${name.value}'")
}

val checkOrganization = taskKey[Unit]("Check organization")
checkOrganization := {
  assert(organization.value == "com.example", s"Expected 'com.example', got '${organization.value}'")
}

val checkVersion = taskKey[Unit]("Check version")
checkVersion := {
  assert(version.value == "1.0.0", s"Expected '1.0.0', got '${version.value}'")
}

val checkScalaVersion = taskKey[Unit]("Check scalaVersion")
checkScalaVersion := {
  assert(scalaVersion.value == "3.7.4", s"Expected '3.7.4', got '${scalaVersion.value}'")
}

val checkScalacOptions = taskKey[Unit]("Check scalacOptions")
checkScalacOptions := {
  val opts = scalacOptions.value
  assert(opts.contains("-deprecation"), s"Expected '-deprecation' in $opts")
  assert(opts.contains("-feature"), s"Expected '-feature' in $opts")
}

val checkDependencies = taskKey[Unit]("Check dependencies")
checkDependencies := {
  val deps = libraryDependencies.value.map(m => s"${m.organization}:${m.name}")
  assert(deps.exists(_.contains("cats-core")), s"Expected cats-core in $deps")
  assert(deps.exists(d => d.contains("scalatest")), s"Expected scalatest in $deps")
}
