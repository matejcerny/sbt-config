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

## Quick Start

Add the plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.github.matejcerny" % "sbt-config" % "0.1.0")
```

Then configure your project in `build.conf`:

```hocon
name = "my-project"
organization = "com.example"
version = "0.1.0-SNAPSHOT"
scalaVersion = "3.3.4"

dependencies = [
  "org.typelevel:cats-core:2.13.0"
]
```

## Documentation

For complete documentation, visit **[matejcerny.github.io/sbt-config](https://matejcerny.github.io/sbt-config/)**.

## License

MIT
