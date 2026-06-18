# Merge / inheritance semantics for shared + module config

In multi-module mode, a project's effective config is `merge(shared, module)`, defined
field-class by field-class:

- **Scalars** (`organization`, `version`, `scalaVersion`, `versionScheme`, `homepage`):
  `module.x orElse shared.x` — the module **overrides** the shared value.
- **Lists** (`dependencies`, `testDependencies`, `scalacOptions`, `resolvers`, `developers`,
  `licenses`): `shared.x ++ module.x` — **append**, with de-duplication left to sbt/coursier as
  it is today.
- **`name`**: `module.name getOrElse moduleKey` — **never** inherited from the root. This keeps
  `crossProject` correct (`coreJVM` and `coreJS` both resolve to `name = "core"`, the right
  artifact name), and a stray top-level `name` is meaningless in multi-module mode (ADR-0012).

There is deliberately **no per-module removal** of an inherited list element in v1. It is
documented rather than supported: if a value should not apply everywhere, don't put it at the
shared level.

## Consequences

- Shared config is additive for lists and overridable for scalars — the intuitive default for a
  "common settings + per-module tweaks" model.
- The append-only list rule keeps `merge` a pure, trivially testable function (ADR-0013) with no
  diff/removal microsyntax to design.
- A future per-module list-removal feature remains possible but is explicitly out of v1 scope.
