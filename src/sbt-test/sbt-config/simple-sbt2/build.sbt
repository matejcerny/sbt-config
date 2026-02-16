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

// Publishing settings assertions
val checkHomepage = taskKey[Unit]("Check homepage")
checkHomepage := {
  val hp = homepage.value
  assert(hp.isDefined, "Expected homepage to be defined")
  assert(hp.get.toString == "https://github.com/example/test-project", s"Expected homepage URL, got '${hp.get}'")
}

val checkLicenses = taskKey[Unit]("Check licenses")
checkLicenses := {
  val lics = licenses.value
  assert(lics.size == 2, s"Expected 2 licenses, got ${lics.size}: $lics")
  val licStrings = lics.map(_.toString)
  assert(licStrings.exists(_.contains("MIT")), s"Expected MIT license in $licStrings")
  assert(licStrings.exists(_.contains("Apache")), s"Expected Apache license in $licStrings")
}

val checkVersionScheme = taskKey[Unit]("Check versionScheme")
checkVersionScheme := {
  val vs = versionScheme.value
  assert(vs.isDefined, "Expected versionScheme to be defined")
  assert(vs.get == "early-semver", s"Expected 'early-semver', got '${vs.get}'")
}

val checkDevelopers = taskKey[Unit]("Check developers")
checkDevelopers := {
  val devs = developers.value
  assert(devs.size == 2, s"Expected 2 developers, got ${devs.size}")

  val dev1 = devs.find(_.id == "dev1")
  assert(dev1.isDefined, "Expected developer with id 'dev1'")
  assert(dev1.get.name == "Developer One", s"Expected name 'Developer One', got '${dev1.get.name}'")
  assert(dev1.get.email == "dev1@example.com", s"Expected email 'dev1@example.com', got '${dev1.get.email}'")

  val dev2 = devs.find(_.id == "dev2")
  assert(dev2.isDefined, "Expected developer with id 'dev2'")
  assert(dev2.get.name == "Developer Two", s"Expected name 'Developer Two', got '${dev2.get.name}'")
}
