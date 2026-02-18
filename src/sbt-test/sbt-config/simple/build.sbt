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
  val deps = libraryDependencies.value
  val depsStr = deps.map(m => s"${m.organization}:${m.name}")
  assert(depsStr.exists(_.contains("cats-core")), s"Expected cats-core in $depsStr")
  assert(depsStr.exists(_.contains("gson")), s"Expected gson in $depsStr")
  assert(depsStr.exists(d => d.contains("scalatest")), s"Expected scalatest in $depsStr")

  // Verify cross-version types
  val catsDep = deps.find(_.name.contains("cats-core")).get
  assert(catsDep.crossVersion.isInstanceOf[sbt.librarymanagement.CrossVersion.Binary], s"Expected cats-core to use Binary cross-version, got ${catsDep.crossVersion}")
  val gsonDep = deps.find(_.name == "gson").get
  assert(gsonDep.crossVersion == sbt.librarymanagement.Disabled(), s"Expected gson to use Disabled cross-version, got ${gsonDep.crossVersion}")
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
  assert(lics.size == 2, s"Expected 2 licenses, got ${lics.size}")
  val licNames = lics.map(_._1)
  assert(licNames.contains("MIT"), s"Expected MIT license in $licNames")
  assert(licNames.contains("Apache-2.0"), s"Expected Apache-2.0 license in $licNames")
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
