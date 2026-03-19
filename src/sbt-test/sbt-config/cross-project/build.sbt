lazy val myProject = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    sbtConfigFile := (ThisBuild / baseDirectory).value / "build.conf"
  )

val checkJvmDeps = taskKey[Unit]("Check JVM project deps")
checkJvmDeps := {
  val deps = (myProject.jvm / libraryDependencies).value

  assert(
    deps.exists(_.name.contains("cats-core")),
    s"JVM project should have cats-core (shared scala dep), got: ${deps.map(_.name)}"
  )
  assert(
    deps.exists(_.name == "gson"),
    s"JVM project should have gson (jvm java dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "epollcat"),
    s"JVM project should NOT have epollcat (native dep), got: ${deps.map(_.name)}"
  )
}

val checkNativeDeps = taskKey[Unit]("Check Native project deps")
checkNativeDeps := {
  val deps = (myProject.native / libraryDependencies).value

  assert(
    deps.exists(_.name.contains("cats-core")),
    s"Native project should have cats-core (shared scala dep), got: ${deps.map(_.name)}"
  )
  assert(
    deps.exists(_.name == "epollcat"),
    s"Native project should have epollcat (native dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "gson"),
    s"Native project should NOT have gson (jvm java dep), got: ${deps.map(_.name)}"
  )
}
