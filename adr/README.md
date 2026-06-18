# Architecture Decision Records

Each file records one decision: the choice made, why, the alternatives rejected, and its
consequences. They live here at the repo root (not under `docs/`, which is the published
website tree). Numbers are stable identifiers in creation order — cross-references use the
number, not the logical order.

| ADR | Decision |
| --- | --- |
| [0001](0001-centralized-config-user-owned-topology.md) | Centralized config, user-owned topology |
| [0002](0002-dependson-stays-in-build-sbt.md) | `dependsOn` / `aggregate` stay in `build.sbt` |
| [0003](0003-single-config-file-at-build-root.md) | One `build.conf`, always at the build root |
| [0004](0004-mode-by-presence-of-module-keys.md) | Mode is determined by the presence of `modules` keys |
| [0005](0005-module-binding-by-project-id.md) | Bind modules to projects by id (exact-first, then strip) |
| [0006](0006-merge-and-inheritance-semantics.md) | Merge / inheritance semantics for shared + module config |
| [0007](0007-settings-precedence-and-escape-hatch.md) | Settings precedence and the custom-settings escape hatch |
| [0008](0008-cross-platform-support.md) | Cross-platform support reuses existing per-component detection |
| [0009](0009-config-model-and-parser.md) | Config model and parser: `BuildConfig` |
| [0010](0010-error-handling-and-diagnostics.md) | Error handling and diagnostics |
| [0011](0011-diagnostic-de-duplication.md) | Two-tier diagnostic de-duplication |
| [0012](0012-top-level-name-in-multi-module-mode.md) | Top-level `name` in multi-module mode warns once |
| [0013](0013-testing-strategy.md) | Testing strategy: unit tests for logic, scripted for wiring |
| [0014](0014-default-config-stub-template.md) | Default config stub points at multi-module docs |
