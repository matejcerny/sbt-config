// Point all sub-projects to the shared root build.conf
val commonSettings = Seq(
  sbtConfigFile := (ThisBuild / baseDirectory).value / "build.conf"
)

lazy val jvmProject = project
  .in(file("jvm"))
  .settings(commonSettings)

lazy val jsProject = project
  .in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)

lazy val nativeProject = project
  .in(file("native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings)

val checkJvmDeps = taskKey[Unit]("Check JVM project deps")
checkJvmDeps := {
  val deps = (jvmProject / libraryDependencies).value

  assert(
    deps.exists(_.name.contains("cats-core")),
    s"JVM project should have cats-core (shared scala dep), got: ${deps.map(_.name)}"
  )
  assert(
    deps.exists(_.name == "gson"),
    s"JVM project should have gson (jvm java dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "scalajs-dom"),
    s"JVM project should NOT have scalajs-dom (js dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "epollcat"),
    s"JVM project should NOT have epollcat (native dep), got: ${deps.map(_.name)}"
  )
}

val checkJsDeps = taskKey[Unit]("Check JS project deps")
checkJsDeps := {
  val deps = (jsProject / libraryDependencies).value

  assert(
    deps.exists(_.name.contains("cats-core")),
    s"JS project should have cats-core (shared scala dep), got: ${deps.map(_.name)}"
  )
  assert(
    deps.exists(_.name == "scalajs-dom"),
    s"JS project should have scalajs-dom (js dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "gson"),
    s"JS project should NOT have gson (jvm java dep), got: ${deps.map(_.name)}"
  )
  assert(
    !deps.exists(_.name == "epollcat"),
    s"JS project should NOT have epollcat (native dep), got: ${deps.map(_.name)}"
  )
}

val checkNativeDeps = taskKey[Unit]("Check Native project deps")
checkNativeDeps := {
  val deps = (nativeProject / libraryDependencies).value

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
  assert(
    !deps.exists(_.name == "scalajs-dom"),
    s"Native project should NOT have scalajs-dom (js dep), got: ${deps.map(_.name)}"
  )
}
