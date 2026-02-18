enablePlugins(ScalaJSPlugin)

val checkCrossVersions = taskKey[Unit]("Check cross-version types for all dependency kinds")
checkCrossVersions := {
  val deps = libraryDependencies.value

  // Scala dep (%%): standard Binary cross-version
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

  // JS dep (%%%): Binary cross-version but different from plain Scala (%%)
  val jsDep = deps.find(_.name == "scalajs-dom").getOrElse(sys.error("scalajs-dom not found"))
  assert(
    jsDep.crossVersion.isInstanceOf[sbt.librarymanagement.CrossVersion.Binary],
    s"Expected scalajs-dom to use Binary cross-version, got ${jsDep.crossVersion}"
  )
  assert(
    scalaDep.crossVersion != jsDep.crossVersion,
    s"JS dependency should have platform-specific cross-version (%%%), but it equals plain Scala (%%): ${jsDep.crossVersion}"
  )
}
