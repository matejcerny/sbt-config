# Settings precedence and the custom-settings escape hatch

Any sbt setting that HOCON cannot express needs no special escape hatch: under the hybrid model
(ADR-0001) the user is already writing `build.sbt`, so a non-HOCON-expressible setting goes
directly on the project definition. No `LocalProject` indirection is required.

For this to compose predictably, the precedence is fixed: the plugin contributes its config via
`projectSettings`, which sbt applies **before** the project's own `.sbt` / settings. Therefore:

- `+=` / `++=` in the user's `build.sbt` **accumulate** on top of the plugin's base, and
- `:=` in the user's `build.sbt` **wins** over the plugin's contribution.

## Consequences

- The "escape hatch" is just `build.sbt` itself — there is nothing extra to learn or document
  beyond this ordering.
- Users can both extend (`+=`) and fully override (`:=`) any plugin-provided setting, with
  deterministic results.
