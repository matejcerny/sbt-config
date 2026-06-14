// Topology lives in build.sbt; configuration (deps, versions) comes from build.conf.
lazy val core = project.in(file("core"))

lazy val skunk = project
  .in(file("modules/skunk"))
  .dependsOn(core)

// Plain JVM project; id ends in a capitalized platform token → must bind by exact-first.
lazy val analyticsJS = project.in(file("analyticsjs"))

// Unlisted project: no `modules.extras` entry → gets no shared config.
lazy val extras = project.in(file("extras"))

val checkCoreName = taskKey[Unit]("core name comes from the module key")
checkCoreName := {
  val n = (core / name).value
  assert(n == "core", s"Expected core name 'core', got '$n'")
}

val checkCoreDeps = taskKey[Unit]("core inherits shared deps only")
checkCoreDeps := {
  val deps = (core / libraryDependencies).value.map(_.name)
  assert(deps.exists(_.contains("cats-core")), s"core should inherit shared cats-core, got: $deps")
  assert(!deps.exists(_.contains("circe-core")), s"core should NOT have skunk's circe-core, got: $deps")
}

val checkSkunkDeps = taskKey[Unit]("skunk merges shared + module deps")
checkSkunkDeps := {
  val deps = (skunk / libraryDependencies).value.map(_.name)
  assert(deps.exists(_.contains("cats-core")), s"skunk should inherit shared cats-core, got: $deps")
  assert(deps.exists(_.contains("circe-core")), s"skunk should have its own circe-core, got: $deps")
}

val checkAnalyticsExactFirst = taskKey[Unit]("analyticsJS binds by exact-first, not stripped")
checkAnalyticsExactFirst := {
  val deps = (analyticsJS / libraryDependencies).value.map(_.name)
  assert(
    deps.exists(_.contains("cats-effect")),
    s"analyticsJS should resolve by exact-first to modules.analyticsJS (cats-effect), got: $deps"
  )
  assert(deps.exists(_.contains("cats-core")), s"analyticsJS should also inherit shared cats-core, got: $deps")
  val n = (analyticsJS / name).value
  assert(n == "analyticsJS", s"Expected name 'analyticsJS', got '$n'")
}

val checkUnlisted = taskKey[Unit]("unlisted project gets no shared config")
checkUnlisted := {
  val deps = (extras / libraryDependencies).value.map(_.name)
  assert(
    !deps.exists(_.contains("cats-core")),
    s"unlisted extras should NOT inherit shared deps, got: $deps"
  )
}
