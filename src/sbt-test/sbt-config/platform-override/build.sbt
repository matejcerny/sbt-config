// No platform plugins present — auto-detection defaults to JVM.
// Override sbtConfigPlatform to simulate a JS project.
sbtConfigPlatform := Platform.Js

val checkDeps = taskKey[Unit]("Check that platform override controls filtering")
checkDeps := {
  val deps = libraryDependencies.value

  // Shared deps always included
  assert(
    deps.exists(_.name.contains("cats-core")),
    s"Should have cats-core (shared scala dep), got: ${deps.map(_.name)}"
  )

  // JS deps included because we overrode platform to Js
  assert(
    deps.exists(_.name == "scalajs-dom"),
    s"Should have scalajs-dom (js dep, platform overridden to Js), got: ${deps.map(_.name)}"
  )

  // JVM deps excluded
  assert(
    !deps.exists(_.name == "gson"),
    s"Should NOT have gson (jvm dep), got: ${deps.map(_.name)}"
  )

  // Native deps excluded
  assert(
    !deps.exists(_.name == "epollcat"),
    s"Should NOT have epollcat (native dep), got: ${deps.map(_.name)}"
  )
}
