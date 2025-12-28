---
sidebar_position: 3
---

# Dependencies

Dependencies are declared in a simple string format and automatically converted to sbt's `libraryDependencies`.

## Format

Dependencies use the format `"organization:artifact:version"`:

```hocon
dependencies = [
  "org.typelevel:cats-core:2.13.0",
  "io.circe:circe-core:0.14.10"
]
```

This is equivalent to the following sbt syntax:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.13.0",
  "io.circe" %% "circe-core" % "0.14.10"
)
```

## Cross-Versioning

All dependencies use Scala cross-versioning (`%%`) by default. This means the Scala version suffix is automatically appended to the artifact name.

## Test Dependencies

Test dependencies are declared separately and automatically get the `Test` scope:

```hocon
testDependencies = [
  "org.scalatest:scalatest:3.2.19",
  "org.scalatestplus:scalacheck-1-18:3.2.19.0"
]
```

This is equivalent to:

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
)
```
