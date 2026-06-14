enablePlugins(ScalaJSPlugin)

val checkCrossVersions = taskKey[Unit]("Check cross-version types for all dependency kinds")
checkCrossVersions := {
  val deps = libraryDependencies.value

  // Shared `scala` dep: in a Scala.js project it is platform cross-versioned (%%%), matching JS deps.
  // .cross(sjs) is still a Binary cross-version, so this assertion holds.
  val scalaDep = deps.find(_.name.contains("cats-core")).getOrElse(sys.error("cats-core not found"))
  assert(
    scalaDep.crossVersion.isInstanceOf[sbt.librarymanagement.CrossVersion.Binary],
    s"Expected cats-core to use Binary cross-version, got ${scalaDep.crossVersion}"
  )

  // Java dep (%): Disabled cross-version
  val javaDep = deps.find(_.name == "gson").getOrElse(sys.error("gson not found"))
  assert(
    javaDep.crossVersion == sbt.librarymanagement.Disabled(),
    s"Expected gson to use Disabled cross-version, got ${javaDep.crossVersion}"
  )

  // JS dep (%%%): Binary cross-version, platform-suffixed via .cross(sjs)
  val jsDep = deps.find(_.name == "scalajs-dom").getOrElse(sys.error("scalajs-dom not found"))
  assert(
    jsDep.crossVersion.isInstanceOf[sbt.librarymanagement.CrossVersion.Binary],
    s"Expected scalajs-dom to use Binary cross-version, got ${jsDep.crossVersion}"
  )
  // In a Scala.js project the shared `scala` dep goes through the same .cross(platformCV) as JS deps,
  // so both carry the identical sjs-suffixed cross-version.
  assert(
    scalaDep.crossVersion == jsDep.crossVersion,
    s"Shared scala dep should be platform cross-versioned (%%%) like JS deps, " +
      s"but ${scalaDep.crossVersion} != ${jsDep.crossVersion}"
  )
}
