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

## Dependency Types

By default, all dependencies use Scala cross-versioning (`%%`). To declare Java dependencies or platform-specific (Scala.js, Scala Native) dependencies, use the nested object format:

```hocon
dependencies {
  scala  = ["org.typelevel:cats-core:2.13.0"]
  java   = ["com.google.code.gson:gson:2.11.0"]
  js     = ["org.scala-js:scalajs-dom:2.8.0"]
  native = ["com.armanbilge:epollcat:0.1.6"]
}
```

All keys are optional — include only the ones you need.

### Key Mapping

| Key      | sbt Operator | Description                                              |
|----------|--------------|----------------------------------------------------------|
| `scala`  | `%%`         | Standard Scala cross-versioned library                   |
| `java`   | `%`          | Plain Java library (no cross-version)                    |
| `js`     | `%%%`        | Scala.js library (requires sbt-scalajs plugin)           |
| `native` | `%%%`        | Scala Native library (requires sbt-scala-native plugin)  |

### Flat vs Nested

The flat list format is equivalent to `{ scala = [...] }`:

```hocon
# These two are equivalent:
dependencies = ["org.typelevel:cats-core:2.13.0"]

dependencies {
  scala = ["org.typelevel:cats-core:2.13.0"]
}
```

You can mix formats between `dependencies` and `testDependencies`:

```hocon
# Nested dependencies with Java libs
dependencies {
  scala = ["org.typelevel:cats-core:2.13.0"]
  java  = ["com.google.code.gson:gson:2.11.0"]
}

# Flat testDependencies (all Scala)
testDependencies = [
  "org.scalatest:scalatest:3.2.19"
]
```

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

Test dependencies also support the nested object format:

```hocon
testDependencies {
  scala = ["org.scalatest:scalatest:3.2.19"]
  java  = ["junit:junit:4.13.2"]
}
```
