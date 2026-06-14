# Multi-module support for `build.conf` — design decisions

Status: **design in progress** (grilling session). This file captures every decision
made so far plus the branches still open, so the session can be resumed.

Phase note: this is an early development phase — **breaking changes are acceptable**,
backward compatibility is explicitly **not** a goal.

---

## Goal

Let a single root `build.conf` describe a multi-module sbt build: shared/common settings
plus per-module settings, with cross-platform (Scala.js / Scala Native / crossProject)
modules fully supported.

---

## Architecture (the core decision)

**Centralized config, user-owned topology** ("hybrid").

- The **plugin owns configuration**: one root `build.conf` supplies deps, versions,
  scalacOptions, publishing metadata, and shared inheritance.
- The **user owns project topology** in a thin `build.sbt`: project/`crossProject`
  existence, base directories, platform plugin enablement, `dependsOn`, `aggregate`.

We explicitly **rejected** having the plugin *generate* projects via
`AutoPlugin.extraProjects`. `extraProjects` exists and works, but generating
`crossProject`s that way is too fragile: it would force Provided deps on
sbt-crossproject + sbt-scalajs + sbt-scala-native (the plugin today has **zero** deps on
them — it sniffs the cross-version prefix string instead), survive their API skew across
versions and across sbt 1.12.8 vs 2.0.0-RC10, and reimplement `CrossProject`'s
platform-matched `dependsOn` wiring by hand. Not worth it; the user writes the
`crossProject` macro (the only robust way) and the plugin augments it.

Example:

```scala
// build.sbt — topology only
lazy val core  = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full).in(file("core"))
lazy val skunk = project.in(file("modules/database/skunk"))
  .dependsOn(core.jvm)
```

```hocon
# build.conf — one file, shared + per-module
scalaVersion = "3.3.4"
dependencies = ["org.typelevel:cats-core:2.13.0"]   # shared, all listed modules

modules {
  core  { dependencies { js = ["org.scala-js:scalajs-dom:2.8.0"] } }
  skunk { dependencies = ["org.tpolecat:skunk-core:0.6.4"] }
}
```

---

## Decisions

### D1 — One file only, always at the build root
- `sbtConfigFile := (LocalRootProject / baseDirectory).value / "build.conf"` — **always**
  the root file. No per-dir fallback, no file-location mode detection.
- Every project reads the one file.
- `createDefaultConfigFile` only ever touches that single root file (idempotent across the
  projects that point at it).
- A stray `build.conf` left in a subproject dir is simply never read.
- The manual `sbtConfigFile := ThisBuild/...` line in the current `cross-project` scripted
  test becomes unnecessary (still works as an override).

### D2 — Mode is determined by at least one `modules.<key>` being present
- **No module keys** (no `modules` block, *or* an empty `modules {}`) → single-project mode:
  top-level config applies to **every** project in the build (preserves today's
  single-project & cross-platform behavior, where each `crossProject` component reads the top
  level and filters by detected platform).
- **≥ 1 `modules.<key>` present** → multi-module mode: top-level = **shared**, applied per
  project by id match; opt-in by listing.
- Keying on the **non-empty parsed map** (`modules.isEmpty`, matching D9) — not on
  `hasPath("modules")` — so a stray/scaffolding empty `modules {}` is forgiving (behaves like
  no block) rather than silently nuking every project's shared config.
- The `modules` block governs **config semantics only**, never file location.

### D3 — Module binding: auto-derive by project id (no boilerplate)
- The plugin derives a project's module key from `thisProject.value.id` using
  **exact-first, then case-sensitive strip-fallback**:
  ```
  key(id) = if (modules.contains(id)) id                       // exact id wins
            else if (modules.contains(strip(id))) strip(id)    // crossProject component
            else <unlisted>
  ```
  - plain project → id is the val name (`skunk`), exact-matches `modules.skunk`
    (path-independent).
  - `crossProject` component → ids `coreJVM`/`coreJS`/`coreNative`; no exact key, so strip
    the trailing platform token → `core` → matches `modules.core`. Each component still
    detects its own platform and filters deps via the **existing** `filterDeps`.
  - **`strip` is case-sensitive** (`JVM`/`JS`/`Native`, capitalized exactly as
    sbt-crossproject emits them). Case-sensitive still catches every real `crossProject`
    component but won't over-strip a plain project like `mathjs`.
  - **Exact-first** means a plain project whose real name ends in a platform token (e.g.
    `analyticsJS`) matches `modules.analyticsJS` directly — **no override needed**. The
    only case exact-first mis-resolves is a nonsensical build with both a `crossProject`
    `core` and a literal `modules.coreJVM` meant for something else.
- **Match on id, not `name`** (the plugin *sets* `name` from config → matching on name is
  circular).
- `sbtConfigModule` survives only as an **optional per-project override** for the rare
  mismatch (e.g. a JVM project literally named `analyticsJS`). Invisible in the common case.
- **Opt-in by listing**: a project gets config iff its id matches a key in `modules`.
  The root aggregator has no `modules.root` entry → gets nothing → stays clean
  automatically (no special "aggregator mode" flag). Module with only shared settings →
  list it empty: `core {}`.
- Exact suffix casing to be pinned by the `scalajs` / `cross-platform` scripted tests.

### D4 — Inheritance / merge semantics (Rule 1)
`merge(shared, module): ProjectConfig`:
- **Scalars** (`organization`, `version`, `scalaVersion`, `versionScheme`, `homepage`):
  `module.x orElse shared.x` (override).
- **Lists** (`dependencies`, `testDependencies`, `scalacOptions`, `resolvers`,
  `developers`, `licenses`): `shared.x ++ module.x` (append; dedup left to sbt/coursier as
  today).
- `name`: `module.name getOrElse moduleKey` — never inherited from the root.
- No per-module *removal* of an inherited list element in v1 (documented; if you need it,
  don't put it at root).

### D5 — `dependsOn` / `aggregate` stay in `build.sbt`
- `dependsOn` is part of the `Project` **definition** (`ClasspathDependency`), not a
  setting. Faking it from `projectSettings` (e.g. `internalDependencyClasspath`) silently
  breaks **published POMs** (`projectDependencies` → `<dependencies>`) and full task/scope
  wiring — fatal for a published multi-module library.
- The platform-qualified target (`core.jvm` vs `core`) is topology HOCON can't express
  without a new microsyntax.
- Therefore: keep `dependsOn`/`aggregate` in `build.sbt`. **Removed from `build.conf`.**
- Deferred option: a definition-time helper `project.in(...).fromConfig` that reads
  `modules.<id>.dependsOn` and applies real `.dependsOn(LocalProject(id))` — but it trades
  the removed binding boilerplate for a new per-project call **and** needs the
  platform-qualified microsyntax. Not in v1.

### D6 — Escape hatch for custom settings
- Trivial under the hybrid: the user is already in `build.sbt`, so any non-HOCON-expressible
  sbt setting goes on the project definition directly. No `LocalProject` indirection needed.
- Settings precedence: plugin contributions (via `projectSettings`) are applied before the
  `.sbt`/project settings, so `+=`/`++=` accumulate on the plugin base and `:=` from the
  user wins.

### D7 — Cross-platform scope
- **Fully supported** via the user's `crossProject` macro + the plugin's **existing**
  per-component platform detection (`detectPlatform` via cross-version prefix) and dep
  filtering (`filterDeps`, `toModuleId`). No new platform-plugin dependencies.
- The nested dependency syntax (`shared`/`jvm`/`js`/`native`, `scala`/`java`) works
  identically at top level and inside each `modules.<key>` (same `DependencyParser` per
  scope).

### D8 — Error handling & diagnostics
- **13a** Missing file → write the commented stub (good first-run UX, unchanged).
  Present-but-malformed (parse error, illegal module id, bad dependency string) →
  **hard load-time failure**. Mechanism: throw **`sbt.MessageOnlyException`**
  (`s"[sbt-config] $error"`) from the load path on cache miss — sbt renders it as a clean
  one-line `[error]`, no stack trace (works in 1.12.x and 2.0.0-RC10). Replaces today's
  `System.err.println` + `None` silent degrade. **Behavior change:** existing single-project
  builds with a malformed file now abort instead of falling back to defaults — acceptable in
  this breaking-changes-OK phase, and correct now that one file drives the whole build.
- **13b** Per-project **warning** when, in multi-module mode, a project's id matches **no**
  `modules.<key>` ("project `skunk` is not configured by sbt-config — no `modules.skunk`
  entry"). Cheap (no build-wide enumeration); catches typos and forgot-to-list from the
  project side. **Suppressed for the root project id** (`LocalRootProject`).
- **13c** Inverse check (a `modules` key with no matching project) needs a build-level
  enumeration / second load-time pass → **deferred**. 13b catches the common typo
  indirectly.

### D9 — Config model & parser
- New top type, `ProjectConfig` reused as the per-project unit:
  ```scala
  case class BuildConfig(shared: ProjectConfig, modules: Map[String, ProjectConfig])
  // single-project file → modules = empty
  ```
- `ConfigParser.parse(file): Either[String, BuildConfig]`.
- Reuse the existing `parseConfig(config: Config): ProjectConfig`:
  - `shared = parseConfig(root)` (it reads named paths; the `modules` object is not a field
    it looks at).
  - `modules = root.getObject("modules").keySet.map(k => k -> parseConfig(root.getConfig("modules").getConfig(k)))`.
- Per-project resolution (one `Def.Initialize[Option[ProjectConfig]]`, replacing the
  per-field `configValue`):
  ```
  if (modules.isEmpty)            => Some(shared)                     // no modules: top-level applies to all
  else modules.get(strip(thisProject.id)) match
         case Some(m)             => Some(merge(shared, m))           // listed module
         case None                => None + warn (unless root)        // unlisted
  ```
- Parse once, cache the `BuildConfig` keyed by file path (reuse existing `configCache`,
  retyped to hold `BuildConfig`). Per-project resolution (merge/strip/match) runs on top of
  the cached `BuildConfig` and is **not** itself cached (it varies by project id).
- The existing field-application code in `configSettings` stays; it just reads from the
  resolved `Option[ProjectConfig]` instead of re-extracting per field.

### D12 — Two-tier diagnostic de-duplication
sbt forces setting initializers repeatedly; diagnostics must dedup or they spam. Two tiers:
- **Per-file** (parse-error throw of D8/13a; top-level `name` warning of D10): emitted
  strictly inside the **cache-miss branch** of `loadConfig`, where `BuildConfig` is first
  parsed and cached → fires exactly once per file, for free.
- **Per-id** (13b unlisted-project warning): the file-keyed cache can't dedup this (all
  projects share one file). Add a dedicated `private val warnedUnlisted = mutable.Set[String]`
  mirroring `configCache`; warn + record on first unlisted resolution of an id, skip if
  present, never add/warn the root id.
- Keeps the existing plain-`mutable`-without-synchronization style (sbt load is effectively
  single-threaded here).

---

### D10 — Top-level `name` in multi-module mode → warn once (resolves Q15)
- The merge default `name = module.name getOrElse moduleKey` is kept (good for
  `crossProject`: `coreJVM`/`coreJS` both → `name = "core"` → correct artifact names).
- A top-level `name` alongside a `modules` block binds to nothing (root unlisted; modules
  default from key). Rather than silently drop it, emit a **one-time load warning**
  (guard `shared.name.isDefined && modules.nonEmpty`), consistent with D8's no-silent-degrade
  stance. `name` is the only scalar meaningless-when-shared, so the only one warranting this.
- Single-project mode is unchanged: top-level `name` = the project name.

### D13 — Primary coverage is pure unit tests; scripted is for wiring only
- Factor the new core logic as **pure functions** next to the existing parser specs:
  - `strip(id): String` — case-sensitive platform-suffix strip (pins D3 casing instantly).
  - `resolveKey(id, keys): Option[String]` — exact-first-then-strip (D3).
  - `merge(shared, module): ProjectConfig` — D4 append/override semantics.
  - `ConfigParser.parse(file): Either[String, BuildConfig]` — D8 errors testable as `Left`.
- Unit tests pin the **full D3/D4/D8 edge-case matrix** (over-strip, exact-first wins,
  list-append vs scalar-override, `name`-never-inherited, malformed → `Left`). Fast, no sbt,
  no per-version run.
- The warning side-effects (13b, D10) are thin wrappers over `resolveKey`'s result, so the
  only thing tests can't cover is the exact log string — trivial, low-risk.
- **Scripted is reserved for integration wiring** (next bullet), not for re-testing logic.

## Open branches (resume here)
### D14 — Scripted integration matrix (resolves Testing strategy)
Scripted proves **wiring only** (logic is unit-tested per D13):
- `multi-module` (sbt 1.x): plain `core` + `skunk` (`skunk.dependsOn(core)`), shared+module
  merge, **plus** an unlisted project (→ no shared deps, behavioral proxy for 13b) and an
  `analyticsJS`-style plain project (→ exact-first, not stripped). Assert via `taskKey`s
  like the existing `checkJvmDeps`.
- `multi-module-cross` (sbt 1.x): `crossProject core` (JVM+Native, reuse `cross-project`
  infra) binds to `modules.core`; per-component platform filtering still works; both
  components get `name="core"`. Pins strip end-to-end.
- `multi-module-sbt2` (3.8.1): JVM-only mirror for sbt 2 parity.
- `malformed` (sbt 1.x): bad dep + scripted `-> reload` expected to **fail** → pins D8.
- **Don't double every scenario on sbt 2** (unit tests cover logic cross-version; one mirror
  suffices). Cross-on-sbt2 **deferred** (blocked on JS/Native plugin readiness — same reason
  current cross tests are 1.x-only).
- **Leave existing scenarios untouched** as single-project-mode regression guards; keep the
  manual `sbtConfigFile := ThisBuild/...` line in `cross-project` (doubles as override-still-
  works proof).
- Per-version run constraint stands: `scripted sbt-config/<name>` and
  `++3.8.1; scripted sbt-config/<name>-sbt2`.
### D15 — Stub template: minimal commented `modules` pointer (resolves Stub template)
- Append ~3 **commented** lines to `createDefaultConfigFile` (stays commented → empty parsed
  map → single-project mode → no behavior change for fresh projects):
  ```hocon
  # Multi-module builds: add a `modules` block (top-level settings become shared).
  # See https://matejcerny.github.io/sbt-config/ — section "Multi-Module".
  # Cross-compilation: see section "Cross-Platform Dependencies".
  ```
- Link the **bare site root** (not a deep `.html` / versioned path — those don't resolve and
  would go stale in generated stubs); name the **section** instead.
- New doc deliverable: a **"Multi-Module"** page in `docs/_docs/` + entry in
  `docs/sidebar.yml` (sibling to the existing "Cross-Platform Dependencies" page).
- **Deferred (post-v1):** `.fromConfig` helper + `dependsOn` microsyntax (D5); build-level
  inverse typo check (D8/13c); per-module single-platform / generated modules (only ever as
  a separate epic, if at all).

---

## Reusable facts established
- `AutoPlugin.extraProjects: Seq[Project]` and `derivedProjects` both exist — not used by
  the chosen design, but confirmed available.
- The plugin currently has **no** dependency on sbt-scalajs / sbt-scala-native /
  sbt-crossproject; platform detection is purely via the cross-version prefix string.
- `LocalProject(id)` / `LocalRootProject` references are the supported way to refer to
  projects/the build root from settings.
