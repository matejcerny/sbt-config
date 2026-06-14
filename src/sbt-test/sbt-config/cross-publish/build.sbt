lazy val root = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)

lazy val rootJVM = root.jvm
lazy val rootNative = root.native

val checkModuleNames = taskKey[Unit]("crossProject artifacts use the configured module name")
checkModuleNames := {
  val jvm = (rootJVM / moduleName).value
  val native = (rootNative / moduleName).value

  assert(jvm == "mylib", s"Expected JVM moduleName 'mylib', got '$jvm'")
  assert(native == "mylib", s"Expected Native moduleName 'mylib', got '$native'")
}

val checkRootSkipped = taskKey[Unit]("auto-generated aggregator root is skipped for publishing")
checkRootSkipped := {
  val skipped = (LocalRootProject / publish / skip).value
  assert(skipped, "Expected LocalRootProject publish / skip to be true")
}
