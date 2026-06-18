# Default config stub points at multi-module docs

`createDefaultConfigFile` appends ~3 **commented** lines pointing at the multi-module
documentation. Because they stay commented, they parse to an empty `modules` map → single-project
mode (ADR-0004) → no behavior change for a fresh project:

```hocon
# Multi-module builds: add a `modules` block (top-level settings become shared).
# See https://matejcerny.github.io/sbt-config/ — section "Multi-Module".
# Cross-compilation: see section "Cross-Platform Dependencies".
```

The link is the **bare site root**, naming the **section** rather than a deep `.html` or
versioned path — those do not resolve from a generated stub and would go stale. The matching doc
deliverable is a **"Multi-Module"** page in `docs/_docs/` plus an entry in `docs/sidebar.yml`,
sibling to the existing "Cross-Platform Dependencies" page.

## Consequences

- First-run users discover multi-module mode from inside their generated `build.conf`, without
  any behavior change to fresh single-project builds.
- Deferred (post-v1): the `.fromConfig` helper + `dependsOn` microsyntax (ADR-0002); the
  build-level inverse typo check (ADR-0010); and per-module single-platform / generated modules
  (only ever as a separate epic, if at all).
