# Cross-platform support reuses existing per-component detection

Cross-platform builds (Scala.js / Scala Native / `crossProject`) are **fully supported** without
adding any platform-plugin dependency. The user writes the `crossProject` macro in `build.sbt`
(ADR-0001); the plugin reuses its **existing** machinery:

- per-component platform detection (`detectPlatform`, via the cross-version prefix string), and
- per-platform dependency filtering (`filterDeps`, `toModuleId`).

Each `crossProject` component binds to one `modules.<key>` by stripping its platform suffix
(ADR-0005), then filters that block's deps to its own platform. The nested dependency syntax
(`shared` / `jvm` / `js` / `native`, and `scala` / `java`) works **identically** at the top
level and inside each `modules.<key>` block, because the same `DependencyParser` runs per scope.

## Consequences

- The plugin retains its zero-dependency-on-platform-plugins property; platform is still sniffed
  from the cross-version prefix, never imported from sbt-crossproject / sbt-scalajs /
  sbt-scala-native.
- Multi-module and cross-platform compose for free: a `modules.core` block drives every
  component of a `crossProject core`, each filtering its own platform deps.
- Cross-platform multi-module on sbt 2 is deferred only by JS/Native plugin readiness, not by
  anything in this design (ADR-0013).
