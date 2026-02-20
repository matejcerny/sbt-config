---
sidebar_position: 6
---

# Cross-Platform Dependencies (Experimental)

:::warning Experimental
Cross-platform dependency support (Scala.js, Scala Native) is experimental. It may not work correctly and may change in future versions.
:::

For projects that target Scala.js or Scala Native, the plugin supports additional dependency keys and platform-aware filtering.

## Setup

Cross-platform projects still require a `build.sbt` to enable platform plugins and define sub-projects — `build.conf` handles only the dependencies and metadata. A minimal setup for a JVM + Scala.js + Scala Native project:

```scala
// project/plugins.sbt
addSbtPlugin("io.github.matejcerny" % "sbt-config" % "@VERSION@")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.2")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.10")
```

```scala
// build.sbt
val commonSettings = Seq(
  sbtConfigFile := (ThisBuild / baseDirectory).value / "build.conf"
)

lazy val jvm = project.in(file("jvm"))
  .settings(commonSettings)

lazy val js = project.in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)

lazy val native = project.in(file("native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings)
```

All three sub-projects share the same `build.conf`; the plugin automatically detects each project's platform and filters dependencies accordingly.

## Language-Split Format

Use `js` and `native` keys alongside `scala` and `java`:

```hocon
dependencies {
  scala  = ["org.typelevel:cats-core:2.13.0"]
  java   = ["com.google.code.gson:gson:2.11.0"]
  js     = ["org.scala-js:scalajs-dom:2.8.0"]
  native = ["com.armanbilge:epollcat:0.1.6"]
}
```

### Key Mapping

| Key      | sbt Operator | Description                                              |
|----------|--------------|----------------------------------------------------------|
| `scala`  | `%%`         | Standard Scala cross-versioned library                   |
| `java`   | `%`          | Plain Java library (no cross-version)                    |
| `js`     | `%%%`        | Scala.js library (requires sbt-scalajs plugin)           |
| `native` | `%%%`        | Scala Native library (requires sbt-scala-native plugin)  |

In this format, `scala` and `java` dependencies are treated as shared (included in all platforms), while `js` and `native` are platform-specific.

## Full Matrix Format

For cross-compiled projects (JVM + Scala.js / Scala Native), use explicit platform grouping:

```hocon
dependencies {
  shared {
    scala = ["org.typelevel:cats-core:2.13.0"]
  }
  jvm {
    scala = ["org.typelevel:cats-effect:3.5.0"]
    java  = ["com.google.code.gson:gson:2.11.0"]
  }
  js     = ["org.scala-js:scalajs-dom:2.8.0"]
  native = ["com.armanbilge:epollcat:0.1.6"]
}
```

The `shared` and `jvm` blocks are objects containing `scala` and/or `java` keys. The `js` and `native` keys are flat lists (all platform-specific deps use `%%%`).

## Platform-Aware Filtering

Dependencies are automatically filtered based on the active platform. The plugin auto-detects which platform a project targets by inspecting the cross-version set by sbt-scalajs or sbt-scala-native:

| Block    | JVM project | Scala.js project | Scala Native project |
|----------|:-----------:|:-----------------:|:--------------------:|
| `shared` | included    | included          | included             |
| `jvm`    | included    | excluded          | excluded             |
| `js`     | excluded    | included          | excluded             |
| `native` | excluded    | excluded          | included             |

### Overriding Platform Detection

The detected platform is exposed as the `sbtConfigPlatform` setting. If auto-detection doesn't work for your setup, you can override it explicitly:

```scala
sbtConfigPlatform := Platform.Js     // or Platform.Native, Platform.Jvm
```

## Test Dependencies

Test dependencies also support all cross-platform formats:

```hocon
testDependencies {
  scala = ["org.scalatest:scalatest:3.2.19"]
  js    = ["org.scala-js:scalajs-test-interface:1.0.0"]
}
```
