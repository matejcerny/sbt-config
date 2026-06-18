# One `build.conf`, always at the build root

There is exactly one configuration file and it is **always** the root file:
`sbtConfigFile := (LocalRootProject / baseDirectory).value / "build.conf"`. Every project in
the build reads that same file. There is no per-directory fallback and no file-location-based
mode detection — a stray `build.conf` left in a subproject directory is simply never read.

This follows from ADR-0001: the plugin owns one centralized config, so a single well-known
location is both sufficient and the thing that makes "one file drives the whole build" true.
Mode (single-project vs multi-module) is decided by the file's *contents*, never its location
— see ADR-0004.

## Consequences

- `createDefaultConfigFile` only ever touches that single root file, so it is idempotent across
  the multiple projects that point at it.
- The manual `sbtConfigFile := ThisBuild / ...` line in the `cross-project` scripted test is now
  unnecessary, but still works as an explicit override (and is kept as a regression proof that
  overriding remains possible).
