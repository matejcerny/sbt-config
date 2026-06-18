# Error handling and diagnostics

How the plugin reacts to configuration problems, in three parts:

**Missing vs malformed.** A *missing* file is good first-run UX: write the commented stub
(ADR-0014) and continue. A *present-but-malformed* file (parse error, illegal module id, bad
dependency string) is a **hard load-time failure**. The mechanism is to throw
`sbt.MessageOnlyException(s"[sbt-config] $error")` from the load path on cache miss — sbt renders
it as a clean one-line `[error]` with no stack trace, on both 1.12.x and 2.0.0. This replaces the
old `System.err.println` + `None` silent degrade. It is a deliberate **behavior change**:
existing single-project builds with a malformed file now abort instead of falling back to
defaults. That is acceptable in this breaking-changes-OK phase, and is correct now that one file
drives the whole build (ADR-0003).

**Unlisted-project warning.** In multi-module mode, when a project's id matches **no**
`modules.<key>`, emit a per-project **warning** (`project 'skunk' is not configured by sbt-config
— no 'modules.skunk' entry`). It is cheap (no build-wide enumeration) and catches typos and
forgot-to-list from the project side. It is **suppressed for the root project id**
(`LocalRootProject`), which is unlisted by design (ADR-0005).

**Inverse check deferred.** Detecting a `modules` key with *no* matching project requires a
build-level enumeration / second load-time pass, so it is **deferred**. The unlisted-project
warning catches the common typo indirectly.

## Consequences

- No more silent degradation: a broken config is loud and immediate.
- Single-project builds that previously limped along on a malformed file will now fail — an
  accepted breaking change.
- Warnings must be de-duplicated to avoid spam under sbt's repeated initializer evaluation — see
  ADR-0011. The top-level `name` warning (ADR-0012) shares the same no-silent-degrade stance.
