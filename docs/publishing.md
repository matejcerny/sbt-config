---
sidebar_position: 4
---

# Publishing Settings

For publishing to Maven Central with [sbt-ci-release](https://github.com/sbt/sbt-ci-release), configure the following fields in your `build.conf`.

## Configuration

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

| Identifier | License                          |
|------------|----------------------------------|
| `Apache2`  | Apache License 2.0               |
| `MIT`      | MIT License                      |
| `CC0`      | Creative Commons Zero            |
| `GPL3`     | GNU General Public License v3.0  |

## Version Schemes

Valid values for `versionScheme`:

| Value         | Description                                               |
|---------------|-----------------------------------------------------------|
| `early-semver`| SemVer with early compatibility (recommended for Scala)   |
| `semver-spec` | Strict SemVer                                             |
| `pvp`         | Package Versioning Policy (Haskell-style)                 |
| `always`      | Always compatible                                         |
| `strict`      | Strict versioning                                         |

## Developer Format

Each developer entry requires all four fields:

| Field   | Description                        |
|---------|------------------------------------|
| `id`    | Developer ID (e.g., GitHub username) |
| `name`  | Full name                          |
| `email` | Email address                      |
| `url`   | Personal/professional URL          |

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
