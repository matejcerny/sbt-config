# sbt-config

An sbt AutoPlugin that reads a single root HOCON file (`build.conf`) and applies it as
project settings (deps, versions, scalacOptions, publishing metadata) across a build,
including cross-platform (Scala.js / Scala Native / `crossProject`) projects.

## Language

**build.conf**:
The single HOCON configuration file at the build root. The plugin owns its contents; the
user owns project topology in `build.sbt`.
_Avoid_: config file (when ambiguous), settings file

**Shared config**:
The top-level fields of `build.conf` (outside any `modules` block). In multi-module mode
they are merged into every listed module; in single-project mode they apply to every project.
_Avoid_: common, global, root config

**Module**:
A unit of per-project configuration declared as a key under the `modules` block
(`modules.skunk { ... }`). Distinct from an sbt project — a module is config; the sbt
project is topology the user writes.
_Avoid_: subproject, component (a `crossProject` *component* is a different thing)

**Module key**:
The string key under `modules` (`skunk`, `core`). A project is bound to a module by matching
its **project id** to a module key (exact-first, then strip the platform suffix).
_Avoid_: module name, project name

**Project id**:
sbt's `thisProject.id` — the val name for a plain project (`skunk`), or val name + platform
suffix for a `crossProject` component (`coreJVM`). Matching is on **id, never `name`**
(the plugin *sets* `name`, so matching on name would be circular).

**Single-project mode**:
`build.conf` has no `modules` block. Top-level config applies to every project. Preserves
the plugin's original behavior.

**Multi-module mode**:
`build.conf` has a `modules` block. Top-level = shared, merged per project by module-key
match. A project gets config only if its id resolves to a listed key (opt-in by listing).

**Platform suffix**:
The `JVM` / `JS` / `Native` token sbt-crossproject appends to a `crossProject` component's
id. Stripped case-sensitively to derive the module key.

## Relationships

- A **build.conf** contains one **Shared config** and zero or more **Modules**
- A **Module** is bound to one or more sbt projects by **Module key** ↔ **Project id** match
- A `crossProject` produces multiple **components** (one per platform), all sharing one
  **Module key** after the **Platform suffix** is stripped
- **Single-project mode** vs **Multi-module mode** is determined solely by presence of the
  `modules` block — never by file location

## Example dialogue

> **Dev:** "If I name the val `core` and cross-build it for JVM and JS, do I write
> `modules.coreJVM` and `modules.coreJS`?"
> **Maintainer:** "No — both components strip their **Platform suffix** to the same
> **Module key** `core`, so you write one `modules.core` block. Each component still filters
> its own platform-specific deps."

## Flagged ambiguities

- "module" was used for both the config block and the sbt project — resolved: a **Module**
  is config under `modules`; the sbt project (and `crossProject` component) is topology the
  user owns in `build.sbt`. They are bound by id↔key matching.
