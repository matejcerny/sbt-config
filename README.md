# sbt-config

[![Latest version](https://maven-badges.sml.io/sonatype-central/io.github.matejcerny/sbt-config_2.12_1.0/badge.svg)](https://repo1.maven.org/maven2/io/github/matejcerny/sbt-config_2.12_1.0)
[![Build Status](https://github.com/matejcerny/sbt-config/actions/workflows/ci.yml/badge.svg)](https://github.com/matejcerny/sbt-config/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/matejcerny/sbt-config/graph/badge.svg?token=MEBX8ZQXGD)](https://codecov.io/gh/matejcerny/sbt-config)

An sbt plugin that allows you to configure your Scala projects using HOCON configuration files instead of `build.sbt`.

## Features

- Configure project metadata (name, organization, version)
- Set Scala version and compiler options
- Declare dependencies in a simple `organization:artifact:version` format
- Automatic cross-version handling for Scala dependencies
- Publishing settings for sbt-ci-release (homepage, licenses, developers)
- Creates a template `build.conf` if one doesn't exist

## Installation

Add the plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.matejcerny" % "sbt-config" % "0.1.0")
```

## Usage

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

# Equivalent to: libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]

# Equivalent to: libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]
```

Your `build.sbt` can be minimal or even empty:

```scala
// All settings come from build.conf
```

## Configuration Reference

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Project name |
| `organization` | String | Organization/group ID |
| `version` | String | Project version |
| `scalaVersion` | String | Scala compiler version |
| `scalacOptions` | Array[String] | Scala compiler options |
| `dependencies` | Array[String] | Compile dependencies |
| `testDependencies` | Array[String] | Test dependencies |
| `homepage` | String | Project homepage URL |
| `licenses` | Array[String] | License identifiers (e.g., "MIT", "Apache2") |
| `versionScheme` | String | Version scheme (e.g., "early-semver") |
| `developers` | Array[Object] | List of project developers |

### Dependency Format

Dependencies are specified as strings in the format:

```
"organization:artifact:version"
```

Examples:
```hocon
dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]
```

All dependencies use Scala cross-versioning (`%%`) by default.

### Publishing Settings

For publishing to Maven Central with [sbt-ci-release](https://github.com/sbt/sbt-ci-release):

```hocon
homepage = "https://github.com/your-org/your-project"
licenses = ["MIT"]
versionScheme = "early-semver"
developers = [
  {
    id = "johndoe",
    name = "John Doe",
    email = "john@example.com",
    url = "https://example.com"
  }
]
```

Supported licenses (matching `sbt.librarymanagement.License`): `Apache2`, `MIT`, `CC0`, `GPL3`

## HOCON Features

Since the configuration uses [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md), you can use its features:

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

## Plugin Settings

The plugin provides one setting:

| Setting         | Type   | Default      | Description                   |
|-----------------|--------|--------------|-------------------------------|
| `sbtConfigFile` | `File` | `build.conf` | Path to the HOCON config file |

To use a different config file:

```scala
sbtConfigFile := baseDirectory.value / "project.conf"
```

## Example Project

See the `example/` directory for a complete example project using sbt-config.

## Development

### Running Tests

```bash
# Unit tests
sbt test

# Integration tests (scripted)
sbt scripted
```

### Publishing Locally

```bash
sbt publishLocal
```

## License

MIT
