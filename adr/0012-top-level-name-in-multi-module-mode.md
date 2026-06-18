# Top-level `name` in multi-module mode warns once

The merge default `name = module.name getOrElse moduleKey` (ADR-0006) is kept, because it is
correct for `crossProject`: `coreJVM` and `coreJS` both resolve to `name = "core"`, the right
artifact name.

A consequence is that a top-level `name` alongside a `modules` block binds to nothing — the root
is unlisted, and modules default their name from the key. Rather than silently drop it, the
plugin emits a **one-time load warning**, guarded by `shared.name.isDefined && modules.nonEmpty`.
This is consistent with the no-silent-degrade stance of ADR-0010. `name` is the only scalar that
is meaningless when shared, so it is the only one that warrants this warning.

Single-project mode is unchanged: a top-level `name` is the project name.

## Consequences

- Authors who mistakenly set a shared `name` are told it has no effect, instead of being puzzled
  by artifact names that ignore it.
- The warning is per-file (fired in the cache-miss branch) and de-duplicated for free
  (ADR-0011).
