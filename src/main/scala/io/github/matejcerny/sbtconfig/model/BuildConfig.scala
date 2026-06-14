package io.github.matejcerny.sbtconfig.model

/** Top-level configuration parsed from a single root `build.conf`.
  *
  * @param shared
  *   Top-level settings. In single-project mode (`modules` empty) these apply to every project; in multi-module mode
  *   they are the shared base merged into each listed module.
  * @param modules
  *   Per-module overrides keyed by module key (matched against a project's id). Empty for single-project files.
  */
case class BuildConfig(shared: ProjectConfig, modules: Map[String, ProjectConfig])

object BuildConfig {

  /** Empty configuration: empty shared settings and no modules. */
  val empty: BuildConfig = BuildConfig(ProjectConfig(), Map.empty)
}
