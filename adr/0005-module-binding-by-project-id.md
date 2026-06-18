# Bind modules to projects by id — exact-first, then strip platform suffix

A project is bound to its `modules.<key>` block by **deriving the key from
`thisProject.value.id`**, with no per-project boilerplate. The derivation is
**exact-first, then a case-sensitive strip-fallback**:

```
key(id) = if (modules.contains(id)) id                    // exact id wins
          else if (modules.contains(strip(id))) strip(id) // crossProject component
          else <unlisted>
```

- A plain project's id is its val name (`skunk`), which exact-matches `modules.skunk` —
  path-independent.
- A `crossProject` component has ids like `coreJVM` / `coreJS` / `coreNative`. There is no exact
  key, so the trailing platform token is stripped → `core` → matches `modules.core`. Each
  component still detects its own platform and filters deps via the existing `filterDeps`
  (ADR-0008).
- **`strip` is case-sensitive** (`JVM` / `JS` / `Native`, exactly as sbt-crossproject emits
  them). Case-sensitivity catches every real `crossProject` component but will not over-strip a
  plain project such as `mathjs`.
- **Exact-first** means a plain project whose name genuinely ends in a platform token (e.g.
  `analyticsJS`) matches `modules.analyticsJS` directly with no override. The only case
  exact-first mis-resolves is a nonsensical build containing both a `crossProject` `core` and a
  literal `modules.coreJVM` meant for something else.

Matching is on **id, never `name`**: the plugin *sets* `name` from config (and `moduleName`),
so matching on name would be circular. Binding is **opt-in by listing** — a project gets config
iff its derived key is present in `modules`. The root aggregator has no `modules.root` entry, so
it gets nothing and stays clean automatically, with no special "aggregator mode" flag; a module
that needs only shared settings is listed empty (`core {}`).

## Consequences

- The common case needs zero binding boilerplate; the val name *is* the binding.
- `sbtConfigModule` survives only as an optional per-project override for the rare real mismatch
  (e.g. a JVM project literally named `analyticsJS` that must bind elsewhere). Invisible
  otherwise.
- The exact suffix casing is pinned by the `scalajs` / `cross-platform` (and `multi-module-cross`)
  scripted tests — see ADR-0013.
