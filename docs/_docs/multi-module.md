# Multi-Module

A single root `build.conf` can configure a whole multi-module build: shared settings at the top level plus per-module overrides in a `modules { … }` block. You keep the project topology (`project`/`crossProject`, `dependsOn`, `aggregate`) in a thin `build.sbt`; the plugin owns the configuration.

## Single-project vs. multi-module mode

The mode is decided by whether the parsed `modules` block contains **at least one key**:

- **No `modules` block** (or an empty `modules {}`) → **single-project mode**: the top-level settings apply to every project in the build. This is the original behavior and is unchanged.
- **One or more `modules.<key>` entries** → **multi-module mode**: the top-level settings become **shared**, and each project gets the shared settings merged with the matching module's overrides. Projects opt in by being listed.

## Shared vs. module settings

```hocon
# build.conf
scalaVersion = "3.3.4"
dependencies = ["org.typelevel:cats-core:2.13.0"]   # shared across all listed modules

modules {
  core {}                                             # only shared settings — listed empty to opt in
  skunk {
    dependencies = ["org.tpolecat:skunk-core:0.6.4"] # shared ++ module
  }
}
```

```scala
// build.sbt — topology only
lazy val core  = project.in(file("core"))
lazy val skunk = project.in(file("modules/skunk")).dependsOn(core)
```

## Binding a project to a module

A project's configuration is found by matching its **id** (the `val` name in `build.sbt`) against the keys in `modules`:

1. **Exact match** wins: a project with id `skunk` binds to `modules.skunk`.
2. Otherwise the plugin **strips a trailing platform suffix** (`JVM`, `JS`, or `Native`) and tries again — so the `crossProject` components `coreJVM` / `coreJS` / `coreNative` all bind to `modules.core`.
3. No match → the project gets **no configuration** (and a one-time warning, except for the root/aggregator project).

The strip is **case-sensitive**, matching the exact casing `sbt-crossproject` emits. A plain project like `mathjs` (lowercase `js`) is never stripped, and a plain project whose id ends in a capitalized token (e.g. `analyticsJS`) binds to `modules.analyticsJS` directly via the exact-first rule — no override needed.

### Overriding the binding

For the rare case where a project's id doesn't match its intended key, set `sbtConfigModule` on the project. When set, it is used as the module key directly (exact lookup, bypassing the strip):

```scala
lazy val analytics = project
  .in(file("analytics"))
  .settings(sbtConfigModule := Some("analyticsJS"))
```

## Merge semantics

When a project binds to a module, its shared and module settings are merged:

| Field group | Fields | Rule |
|-------------|--------|------|
| Scalars | `organization`, `version`, `scalaVersion`, `versionScheme`, `homepage` | module value overrides shared (`module orElse shared`) |
| Lists | `dependencies`, `testDependencies`, `scalacOptions`, `resolvers`, `developers`, `licenses` | appended (`shared ++ module`); deduplication is left to sbt/coursier |
| `name` | `name` | the module's `name`, or the **module key** — never inherited from the top level |

Because `name` defaults to the module key, a `crossProject` whose components are `coreJVM`/`coreNative` both get `name = "core"`, producing correct artifact names. A top-level `name` alongside a `modules` block binds to nothing and is ignored with a one-time warning.

There is no per-module *removal* of an inherited list element — if a value shouldn't be everywhere, don't put it at the top level.

## Cross-platform modules

Cross-compilation works exactly as in single-project mode (see [Cross-Platform Dependencies](cross-platform.md)). Write the `crossProject` macro in `build.sbt`; the plugin detects each component's platform and filters its dependencies. The nested `shared`/`jvm`/`js`/`native` dependency syntax is available inside each `modules.<key>` block:

```hocon
modules {
  core {
    dependencies {
      shared { scala = ["org.typelevel:cats-core:2.13.0"] }
      jvm    { java  = ["com.google.code.gson:gson:2.11.0"] }
      native = ["com.armanbilge:epollcat:0.1.6"]
    }
  }
}
```

## Topology stays in `build.sbt`

`dependsOn` and `aggregate` are part of a project's **definition**, not settings, and the platform-qualified target (`core.jvm` vs `core`) can't be expressed in HOCON. They therefore live in `build.sbt`:

```scala
lazy val core  = crossProject(JVMPlatform, NativePlatform).crossType(CrossType.Full).in(file("core"))
lazy val skunk = project.in(file("modules/skunk")).dependsOn(core.jvm)
lazy val root  = project.in(file(".")).aggregate(core.jvm, core.native, skunk)
```

Any sbt setting that HOCON can't express goes directly on the project definition in `build.sbt`.

## Malformed configuration aborts the load

A missing `build.conf` is created as a commented stub (single-project mode, no behavior change). A **present-but-malformed** file — a parse error or an invalid dependency string, at the top level or inside any module — **aborts the build load** with a single `[sbt-config]` error rather than silently degrading to defaults.
