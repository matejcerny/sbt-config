# Two-tier diagnostic de-duplication

sbt forces setting initializers repeatedly, so any diagnostic emitted from an initializer will
spam unless it de-duplicates. Diagnostics are split into two tiers, each de-duplicated at the
right granularity:

- **Per-file** diagnostics — the parse-error throw (ADR-0010) and the top-level `name` warning
  (ADR-0012) — are emitted strictly inside the **cache-miss branch** of `loadConfig`, where
  `BuildConfig` is first parsed and cached (ADR-0009). They therefore fire exactly once per file,
  for free.
- **Per-id** diagnostics — the unlisted-project warning (ADR-0010) — cannot use the file-keyed
  cache, because every project shares the one file. A dedicated `private val warnedUnlisted =
  mutable.Set[String]` mirrors `configCache`: warn and record on the first unlisted resolution of
  an id, skip if already present, and never add or warn the root id.

## Consequences

- Each distinct problem is reported once, regardless of how many times sbt re-evaluates the
  initializers.
- Keeps the existing plain-`mutable`-without-synchronization style, which is safe because sbt
  load is effectively single-threaded here.
