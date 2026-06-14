package io.github.matejcerny.sbtconfig

import io.github.matejcerny.sbtconfig.model.ProjectConfig

/** Pure resolution logic mapping a project id to a module config (D3) and merging shared + per-module settings (D4). */
object ModuleResolver {

  // Platform suffixes emitted by sbt-crossproject components, in the exact casing they use.
  private val platformSuffixes = Seq("JVM", "JS", "Native")

  /** Strip a trailing platform suffix (`JVM`/`JS`/`Native`) from a crossProject component id.
    *
    * Case-sensitive on purpose: `coreJVM`/`coreJS`/`coreNative` -> `core`, but a plain `mathjs` (lowercase `js`) is
    * left untouched.
    */
  def strip(id: String): String =
    platformSuffixes
      .collectFirst {
        case suffix if id.endsWith(suffix) && id.length > suffix.length => id.dropRight(suffix.length)
      }
      .getOrElse(id)

  /** Resolve the module key for a project id: exact id wins, then the stripped (crossProject) id, else None. */
  def resolveKey(id: String, keys: Set[String]): Option[String] =
    if (keys.contains(id)) Some(id)
    else {
      val stripped = strip(id)
      if (stripped != id && keys.contains(stripped)) Some(stripped)
      else None
    }

  /** Merge shared settings with a module's settings (D4).
    *
    * Scalars override (`module orElse shared`); lists append (`shared ++ module`); `name` is never inherited from
    * shared and defaults to the module key.
    */
  def merge(shared: ProjectConfig, module: ProjectConfig, moduleKey: String): ProjectConfig = {
    def append[A](s: Option[Seq[A]], m: Option[Seq[A]]): Option[Seq[A]] =
      (s, m) match {
        case (None, None)         => None
        case (Some(xs), None)     => Some(xs)
        case (None, Some(ys))     => Some(ys)
        case (Some(xs), Some(ys)) => Some(xs ++ ys)
      }

    ProjectConfig(
      name = module.name.orElse(Some(moduleKey)),
      organization = module.organization.orElse(shared.organization),
      version = module.version.orElse(shared.version),
      scalaVersion = module.scalaVersion.orElse(shared.scalaVersion),
      scalacOptions = append(shared.scalacOptions, module.scalacOptions),
      dependencies = append(shared.dependencies, module.dependencies),
      testDependencies = append(shared.testDependencies, module.testDependencies),
      homepage = module.homepage.orElse(shared.homepage),
      licenses = append(shared.licenses, module.licenses),
      versionScheme = module.versionScheme.orElse(shared.versionScheme),
      developers = append(shared.developers, module.developers),
      resolvers = append(shared.resolvers, module.resolvers)
    )
  }
}
