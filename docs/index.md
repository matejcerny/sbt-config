---
sidebar_position: 1
slug: /
---

# Getting Started

sbt-config is an sbt plugin that allows you to configure your Scala projects using HOCON configuration files instead of `build.sbt`.

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

dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]

testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]
```

Your `build.sbt` can be minimal or even empty - all settings come from `build.conf`.

## Features

- **Project metadata** - Configure name, organization, and version
- **Scala settings** - Set Scala version and compiler options
- **Dependencies** - Declare dependencies in a simple `organization:artifact:version` format
- **Cross-versioning** - Automatic handling of Scala cross-version dependencies
- **Publishing** - Configure homepage, licenses, and developers for Maven Central publishing
- **Template generation** - Creates a commented `build.conf` template if one doesn't exist
