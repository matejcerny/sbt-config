# Config model and parser: `BuildConfig` with reused `ProjectConfig`

The config model gains a top type that reuses the existing per-project unit:

```scala
case class BuildConfig(shared: ProjectConfig, modules: Map[String, ProjectConfig])
// a single-project file parses to modules = empty
```

`ConfigParser.parse(file): Either[String, BuildConfig]` is the single entry point, built on the
existing `parseConfig(config: Config): ProjectConfig`:

- `shared = parseConfig(root)` — it reads named paths, and the `modules` object is not one of
  the fields it looks at.
- `modules = root.getObject("modules").keySet.map(k => k -> parseConfig(root.getConfig("modules").getConfig(k)))`.

Per-project resolution is a single `Def.Initialize[Option[ProjectConfig]]` that replaces the old
per-field `configValue`:

```
if (modules.isEmpty)            => Some(shared)               // no modules: top-level applies to all
else modules.get(strip(thisProject.id)) match
       case Some(m)             => Some(merge(shared, m))     // listed module
       case None                => None + warn (unless root)  // unlisted (ADR-0010)
```

The parsed `BuildConfig` is parsed once and cached keyed by file path (the existing
`configCache`, retyped). Per-project resolution (merge / strip / match, ADR-0005 and ADR-0006)
runs on top of the cached `BuildConfig` and is **not** itself cached, because it varies by
project id.

## Consequences

- Reuses `parseConfig` and `configCache` wholesale; the field-application code in
  `configSettings` is unchanged except that it reads from the resolved `Option[ProjectConfig]`
  instead of re-extracting per field.
- Parse errors are representable as `Left`, which makes the error path (ADR-0010) unit-testable
  without sbt (ADR-0013).
- The file-keyed cache is the natural place to fire once-per-file diagnostics (ADR-0011).
