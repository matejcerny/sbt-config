# Mode is determined by the presence of `modules` keys

Whether a build is in **single-project mode** or **multi-module mode** is decided solely by
whether `build.conf` contains at least one `modules.<key>`:

- **No module keys** (no `modules` block, *or* an empty `modules {}`) → single-project mode:
  top-level config applies to **every** project in the build. This preserves the plugin's
  original single-project and cross-platform behavior, where each `crossProject` component reads
  the top level and filters by its detected platform.
- **≥ 1 `modules.<key>` present** → multi-module mode: top-level config becomes **shared**,
  applied per project by id match (opt-in by listing — see ADR-0005).

The test is on the **non-empty parsed map** (`modules.isEmpty`, per the model in ADR-0009), not
on `Config.hasPath("modules")`. This makes a stray or scaffolding empty `modules {}` forgiving:
it behaves like no block rather than silently nuking every project's shared config. The
`modules` block governs **config semantics only**, never file location (ADR-0003).

## Consequences

- Existing single-project and cross-platform builds keep working untouched — they simply have no
  `modules` block.
- Multi-module mode is strictly opt-in: adding the first `modules.<key>` is the switch.
- A user can scaffold an empty `modules {}` without breaking their build's shared config.
