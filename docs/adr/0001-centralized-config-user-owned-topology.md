# Centralized config, user-owned topology

For multi-module builds, the plugin owns **configuration** (one root `build.conf`: deps,
versions, scalacOptions, publishing metadata, shared inheritance) while the user owns
**project topology** in a thin `build.sbt` (project/`crossProject` existence, base
directories, platform plugin enablement, `dependsOn`, `aggregate`). The plugin augments the
projects the user defines; it binds config to them by matching project id to a `modules.<key>`.

We explicitly **rejected** generating projects via `AutoPlugin.extraProjects` (which exists and
works). Generating `crossProject`s that way would force `Provided` deps on
sbt-crossproject + sbt-scalajs + sbt-scala-native — the plugin today has **zero** dependency on
them (it detects platform purely from the cross-version prefix string) — survive those plugins'
API skew across versions and across sbt 1.12.x vs 2.0.x, and reimplement `CrossProject`'s
platform-matched `dependsOn` wiring by hand. The user writing the `crossProject` macro is the
only robust way; the plugin augments it.

## Consequences

- The plugin keeps its zero-dependency-on-platform-plugins property (cross-version prefix
  sniffing stays the platform detection mechanism).
- Topology that HOCON can't express (platform-qualified `dependsOn` like `core.jvm`, base
  dirs) lives in `build.sbt` — see ADR-0002.
- Custom non-HOCON sbt settings need no escape hatch: the user is already in `build.sbt`.
