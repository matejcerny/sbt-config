// Negative test for the aggregator publish-skip heuristic. The plugin skips publishing only when a
// project is BOTH the build root AND aggregates other projects. This build exercises the two cases
// the heuristic must NOT skip, locking in that it stays conservative:
//
//   - `root`: an explicitly-defined build root with no `.aggregate(...)` — a real publishable
//     artifact, so being the root alone must not skip it.
//   - `sub`: a non-root subproject — never the build root, so it must always remain publishable.

lazy val root = project.in(file("."))

lazy val sub = project.in(file("sub"))

val checkRootPublishable = taskKey[Unit]("explicit non-aggregating root is not skipped for publishing")
checkRootPublishable := {
  val skipped = (root / publish / skip).value
  assert(!skipped, s"Expected root publish / skip to be false (root has no aggregates), got $skipped")
}

val checkSubPublishable = taskKey[Unit]("non-root subproject is not skipped for publishing")
checkSubPublishable := {
  val skipped = (sub / publish / skip).value
  assert(!skipped, s"Expected sub publish / skip to be false (not the build root), got $skipped")
}
