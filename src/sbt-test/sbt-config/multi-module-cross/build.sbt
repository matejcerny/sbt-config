// crossProject component ids are coreJVM / coreNative; both strip to `core` and bind to modules.core.
lazy val core = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("core"))

val checkJvmDeps = taskKey[Unit]("JVM component: shared + jvm deps, native filtered out, name = core")
checkJvmDeps := {
  val deps = (core.jvm / libraryDependencies).value.map(_.name)
  assert(deps.exists(_.contains("cats-core")), s"coreJVM should have cats-core (shared), got: $deps")
  assert(deps.exists(_ == "gson"), s"coreJVM should have gson (jvm java), got: $deps")
  assert(!deps.exists(_ == "epollcat"), s"coreJVM should NOT have epollcat (native), got: $deps")

  val n = (core.jvm / name).value
  assert(n == "core", s"Expected coreJVM name 'core', got '$n'")
}

val checkNativeDeps = taskKey[Unit]("Native component: shared + native deps, jvm filtered out, name = core")
checkNativeDeps := {
  val deps = (core.native / libraryDependencies).value.map(_.name)
  assert(deps.exists(_.contains("cats-core")), s"coreNative should have cats-core (shared), got: $deps")
  assert(deps.exists(_ == "epollcat"), s"coreNative should have epollcat (native), got: $deps")
  assert(!deps.exists(_ == "gson"), s"coreNative should NOT have gson (jvm java), got: $deps")

  val n = (core.native / name).value
  assert(n == "core", s"Expected coreNative name 'core', got '$n'")
}
