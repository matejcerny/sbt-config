# Resolvers

Configure additional Maven repositories for dependency resolution.

## Configuration

Add resolvers to your `build.conf`:

```hocon
resolvers = [
  { 
    name = "Sonatype Snapshots"
    url = "https://central.sonatype.com/repository/maven-snapshots/" 
  }
]
```

Each resolver requires two fields:

| Field  | Description            |
|--------|------------------------|
| `name` | Repository name        |
| `url`  | Repository URL         |

## How It Works

Each resolver entry translates to an sbt `MavenRepository`:

```scala
// Generated from build.conf
resolvers += MavenRepository("Sonatype Snapshots", "https://central.sonatype.com/repository/maven-snapshots/")
```

## Multiple Resolvers

```hocon
resolvers = [
  { 
    name = "Sonatype Snapshots"
    url = "https://central.sonatype.com/repository/maven-snapshots/" 
  },
  {
    name = "Sonatype Releases"
    url = "https://central.sonatype.com/repository/maven-releases/" 
  }
]
```

## Using HOCON Features

You can use [HOCON substitutions](./hocon.md) to avoid repetition:

```hocon
sonatype-base = "https://central.sonatype.com/repository"

resolvers = [
  { name = "Sonatype Snapshots", url = ${sonatype-base}"/maven-snapshots/" },
  { name = "Sonatype Releases", url = ${sonatype-base}"/maven-releases/" }
]
```
