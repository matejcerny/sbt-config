# sbt-config

An sbt plugin that allows you to configure your Scala projects using HOCON configuration files instead of `build.sbt`.

## Installation

Add the plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.matejcerny" % "sbt-config" % "@VERSION@")
```

## Quick Start

After adding the plugin, run any sbt command (e.g., `sbt compile`). The plugin will automatically create a `build.conf` template file with all supported fields commented out.

Edit `build.conf` to configure your project:

```hocon
name = "my-project"
organization = "com.example"
version = "0.1.0-SNAPSHOT"

scalaVersion = "3.3.4"

scalacOptions = [
  "-deprecation",
  "-feature",
  "-unchecked"
]

# Dependencies (format: "organization:artifact:version")
dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]

# Test dependencies (automatically added with Test scope)
testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]
```

Your `build.sbt` can be minimal or even empty - all settings come from `build.conf`.

## Configuration Reference

| Field              | Type          | Description                                               |
|--------------------|---------------|-----------------------------------------------------------|
| `name`             | String        | Project name                                              |
| `organization`     | String        | Organization/group ID                                     |
| `version`          | String        | Project version                                           |
| `scalaVersion`     | String        | Scala compiler version                                    |
| `scalacOptions`    | Array[String] | Scala compiler options                                    |
| `dependencies`     | Array[String] | Compile dependencies                                      |
| `testDependencies` | Array[String] | Test dependencies                                         |
| `homepage`         | String        | Project homepage URL                                      |
| `licenses`         | Array[String] | License identifiers (e.g. "Apache2", "CC0", "GPL3, "MIT") |
| `versionScheme`    | String        | Version scheme (e.g., "early-semver", "semver-spec")      |
| `developers`       | Array[Object] | List of project developers                                |

## Dependency Format

Dependencies are specified as strings in the format `"organization:artifact:version"`.

All dependencies use Scala cross-versioning (`%%`) by default.

```hocon
dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]
```

## HOCON Features

Since the configuration uses [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md), you can use its powerful features:

### Variable Substitution

```hocon
base-version = "1.0"
version = ${base-version}".0-SNAPSHOT"
```

### Comments

```hocon
# This is a comment
name = "my-project"  // This is also a comment
```

### Multiline Arrays

```hocon
scalacOptions = [
  "-deprecation",
  "-feature",
  "-unchecked"
]
```

## Publishing Settings

For publishing to Maven Central with [sbt-ci-release](https://github.com/sbt/sbt-ci-release), configure the following:

```hocon
homepage = "https://github.com/your-org/your-project"

licenses = ["MIT"]  # or "Apache-2.0", "GPL-3.0", etc.

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

### Supported Licenses

The following license identifiers are supported (matching `sbt.librarymanagement.License`):

- `Apache2` - Apache License 2.0
- `MIT` - MIT License
- `CC0` - Creative Commons Zero
- `GPL3` - GNU General Public License v3.0

### Version Schemes

Valid values for `versionScheme`:

- `early-semver` - SemVer with early compatibility (recommended for Scala projects)
- `semver-spec` - Strict SemVer
- `pvp` - Package Versioning Policy (Haskell-style)
- `always` - Always compatible
- `strict` - Strict versioning

## Plugin Settings

To use a different config file location:

```scala
sbtConfigFile := baseDirectory.value / "project.conf"
```
