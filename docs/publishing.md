---
sidebar_position: 4
---

# Publishing Settings

Configure your project for publishing to Maven Central using [sbt-ci-release](https://github.com/sbt/sbt-ci-release).

## Prerequisites

The publishing settings require the **sbt-ci-release** plugin. Add it to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "VERSION")
```

This plugin handles:
- Automated versioning from git tags
- GPG signing
- Publishing to Sonatype/Maven Central
- GitHub Actions integration

See the [sbt-ci-release documentation](https://github.com/sbt/sbt-ci-release) for setup instructions.

## Configuration

Add the following to your `build.conf`:

```hocon
homepage = "https://github.com/your-org/your-project"

licenses = ["MIT"]

versionScheme = "early-semver"

developers = [
  {
    id = "johndoe"
    name = "John Doe"
    email = "john@example.com"
    url = "https://johndoe.dev"
  }
]
```

## Supported Licenses

The following license identifiers are supported, matching `sbt.librarymanagement.License`:

| Identifier | License                         |
|------------|---------------------------------|
| `Apache2`  | Apache License 2.0              |
| `MIT`      | MIT License                     |
| `CC0`      | Creative Commons Zero           |
| `GPL3`     | GNU General Public License v3.0 |

## Version Schemes

Valid values for `versionScheme`:

| Value          | Description                                             |
|----------------|---------------------------------------------------------|
| `early-semver` | SemVer with early compatibility (recommended for Scala) |
| `semver-spec`  | Strict SemVer                                           |
| `pvp`          | Package Versioning Policy (Haskell-style)               |
| `always`       | Always compatible                                       |
| `strict`       | Strict versioning                                       |

## Developer Format

Each developer entry requires all four fields:

| Field   | Description                          |
|---------|--------------------------------------|
| `id`    | Developer ID (e.g., GitHub username) |
| `name`  | Full name                            |
| `email` | Email address                        |
| `url`   | Personal/professional URL            |

Multiple developers can be specified:

```hocon
developers = [
  {
    id = "dev1"
    name = "Developer One"
    email = "dev1@example.com"
    url = "https://dev1.example.com"
  },
  {
    id = "dev2"
    name = "Developer Two"
    email = "dev2@example.com"
    url = "https://dev2.example.com"
  }
]
```

## Complete Example

A full `build.conf` with publishing settings:

```hocon
name = "my-library"
organization = "com.example"
scalaVersion = "3.3.4"

dependencies = [
  "org.typelevel:cats-core:2.13.0"
]

testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]

# Publishing (requires sbt-ci-release)
homepage = "https://github.com/example/my-library"
licenses = ["Apache2"]
versionScheme = "early-semver"
developers = [
  {
    id = "maintainer"
    name = "Project Maintainer"
    email = "maintainer@example.com"
    url = "https://example.com"
  }
]
```
