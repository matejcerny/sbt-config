---
sidebar_position: 5
---

# HOCON Features

Since the configuration uses [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md) (Human-Optimized Config Object Notation), you can leverage its powerful features.

## Comments

HOCON supports two comment styles:

```hocon
# Hash-style comment
name = "my-project"  // C-style comment
```

## Multiline Arrays

Arrays can span multiple lines for better readability:

```hocon
scalacOptions = [
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint"
]
```

## Variable Substitution

Reference other values in your configuration:

```hocon
base-version = "1.0"
version = ${base-version}".0-SNAPSHOT"
```

## String Concatenation

Strings can be concatenated without explicit operators:

```hocon
org = "com.example"
name = ${org}".myproject"
```

## Optional Substitution

Use `${?var}` for optional substitutions that won't fail if the variable is missing:

```hocon
version = ${?BUILD_VERSION}
```

## Includes

Include other configuration files:

```hocon
include "common.conf"

name = "my-project"
```

## Unquoted Strings

Simple strings don't require quotes:

```hocon
name = my-project
scalaVersion = 3.3.4
```

However, strings with special characters need quotes:

```hocon
organization = "com.example"
homepage = "https://example.com"
```

## Learn More

For the complete HOCON specification, see the [official documentation](https://github.com/lightbend/config/blob/main/HOCON.md).
