# `dependsOn` / `aggregate` stay in `build.sbt`, not `build.conf`

Inter-project wiring (`dependsOn`, `aggregate`) is **removed from `build.conf`** and lives only
in the user's `build.sbt`. A reader might expect a config-driven plugin to express dependencies
in the config — this records why it deliberately does not.

`dependsOn` is part of the `Project` **definition** (a `ClasspathDependency`), not a setting.
Faking it from `projectSettings` (e.g. by injecting `internalDependencyClasspath`) silently
breaks **published POMs** — `projectDependencies` would not feed `<dependencies>` — and the full
task/scope wiring. That is fatal for a published multi-module library. Additionally, the
platform-qualified target (`core.jvm` vs `core`) is topology HOCON cannot express without
inventing a new microsyntax.

## Consequences

- Multi-module users write real `.dependsOn(...)` / `.aggregate(...)` in `build.sbt`; the plugin
  only contributes settings. Follows from ADR-0001.
- Deferred (post-v1): a definition-time `project.in(...).fromConfig` helper reading
  `modules.<id>.dependsOn` — but it trades binding boilerplate for a new per-project call **and**
  still needs the platform-qualified microsyntax, so it is out of v1 scope.
