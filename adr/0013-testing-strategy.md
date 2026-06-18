# Testing strategy: pure unit tests for logic, scripted for wiring

**Primary coverage is pure unit tests.** The new core logic is factored as pure functions next to
the existing parser specs, and the full edge-case matrix is pinned there — fast, no sbt, no
per-version run:

- `strip(id): String` — case-sensitive platform-suffix strip (pins the ADR-0005 casing instantly).
- `resolveKey(id, keys): Option[String]` — exact-first-then-strip (ADR-0005).
- `merge(shared, module): ProjectConfig` — append/override semantics (ADR-0006).
- `ConfigParser.parse(file): Either[String, BuildConfig]` — error paths testable as `Left`
  (ADR-0009, ADR-0010).

The unit tests cover over-strip, exact-first-wins, list-append vs scalar-override,
`name`-never-inherited, and malformed → `Left`. The warning side-effects (the unlisted-project
warning and the top-level `name` warning) are thin wrappers over `resolveKey`'s result, so the
only thing unit tests cannot assert is the exact log string — trivial and low-risk.

**Scripted is reserved for integration wiring**, not for re-testing logic:

- `multi-module` (sbt 1.x): plain `core` + `skunk` (`skunk.dependsOn(core)`), shared+module
  merge, **plus** an unlisted project (no shared deps — a behavioral proxy for the
  unlisted-project warning) and an `analyticsJS`-style plain project (exact-first, not stripped).
  Asserted via `taskKey`s like the existing `checkJvmDeps`.
- `multi-module-cross` (sbt 1.x): a `crossProject core` (JVM+Native, reusing the `cross-project`
  infra) binds to `modules.core`; per-component platform filtering still works; both components
  get `name = "core"`. Pins strip end-to-end.
- `multi-module-sbt2` (Scala 3 / sbt 2): a JVM-only mirror for sbt 2 parity.
- `malformed` (sbt 1.x): a bad dep with a scripted `-> reload` expected to **fail**, pinning the
  fail-fast path of ADR-0010.

Scenarios are deliberately **not** doubled on sbt 2 — unit tests cover the logic cross-version,
so one mirror suffices; cross-on-sbt2 is deferred behind JS/Native plugin readiness (ADR-0008).
Existing single-project scenarios are left untouched as regression guards, and the manual
`sbtConfigFile := ThisBuild / ...` line in `cross-project` is kept as an override-still-works
proof (ADR-0003).

## Consequences

- The expensive, slow, per-version scripted runs stay small and prove only wiring; logic
  regressions are caught by fast unit tests.
- `+scripted` cannot run across sbt versions: the sbt 1.x scenarios live under `sbt-config/*` and
  the sbt 2 mirror under a separate `sbt-config-sbt2/*` group, run per version.
