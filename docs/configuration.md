---
sidebar_position: 2
---

# Configuration Reference

All configuration fields are optional. The plugin will use sbt defaults for any field not specified.

## Available Fields

| Field              | Type          | Description                                          |
|--------------------|---------------|------------------------------------------------------|
| `name`             | String        | Project name                                         |
| `organization`     | String        | Organization/group ID                                |
| `version`          | String        | Project version                                      |
| `scalaVersion`     | String        | Scala compiler version                               |
| `scalacOptions`    | Array[String] | Scala compiler options                               |
| `dependencies`     | Array[String] | Compile dependencies                                 |
| `testDependencies` | Array[String] | Test dependencies                                    |
| `homepage`         | String        | Project homepage URL                                 |
| `licenses`         | Array[String] | License identifiers (e.g., "MIT", "Apache2")         |
| `versionScheme`    | String        | Version scheme (e.g., "early-semver", "semver-spec") |
| `developers`       | Array[Object] | List of project developers                           |

## Example

A complete `build.conf` example:

```hocon
name = "my-project"
organization = "com.example"
version = "1.0.0"

scalaVersion = "3.3.4"

scalacOptions = [
  "-deprecation",
  "-feature",
  "-unchecked"
]

dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]

testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]

homepage = "https://github.com/example/my-project"
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

## Plugin Settings

The plugin provides one sbt setting to customize the config file location:

| Setting         | Type   | Default      | Description                   |
|-----------------|--------|--------------|-------------------------------|
| `sbtConfigFile` | `File` | `build.conf` | Path to the HOCON config file |

To use a different config file:

```scala
sbtConfigFile := baseDirectory.value / "project.conf"
```
