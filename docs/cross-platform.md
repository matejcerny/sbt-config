---
sidebar_position: 6
---

# Cross-Platform Dependencies (Experimental)

:::warning Experimental
Cross-platform dependency support (Scala.js, Scala Native) is experimental. It may not work correctly and may change in future versions.
:::

For projects that target Scala.js or Scala Native, the plugin supports additional dependency keys and platform-aware filtering.

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

Dependencies are automatically filtered based on the active platform:

| Block    | JVM project | Scala.js project | Scala Native project |
|----------|:-----------:|:-----------------:|:--------------------:|
| `shared` | included    | included          | included             |
| `jvm`    | included    | excluded          | excluded             |
| `js`     | excluded    | included          | included             |
| `native` | excluded    | included          | included             |

## Test Dependencies

Test dependencies also support all cross-platform formats:

```hocon
testDependencies {
  scala = ["org.scalatest:scalatest:3.2.19"]
  js    = ["org.scala-js:scalajs-test-interface:1.0.0"]
}
```
