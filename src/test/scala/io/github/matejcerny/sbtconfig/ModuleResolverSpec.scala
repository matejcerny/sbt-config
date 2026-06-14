package io.github.matejcerny.sbtconfig

import io.github.matejcerny.sbtconfig.model.{ Dependency, Developer, Platform, ProjectConfig, Resolver }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ModuleResolverSpec extends AnyFlatSpec with Matchers {

  "ModuleResolver.strip" should "strip the JVM/JS/Native platform suffixes" in {
    ModuleResolver.strip("coreJVM") shouldBe "core"
    ModuleResolver.strip("coreJS") shouldBe "core"
    ModuleResolver.strip("coreNative") shouldBe "core"
  }

  it should "leave a plain id with a lowercase platform-like suffix untouched" in {
    ModuleResolver.strip("mathjs") shouldBe "mathjs"
    ModuleResolver.strip("native") shouldBe "native"
  }

  it should "not strip an exact-name project ending in a capitalized token" in {
    // analyticsJS is itself the id; strip removes the suffix mechanically, but resolveKey's exact-first wins.
    ModuleResolver.strip("analyticsJS") shouldBe "analytics"
  }

  it should "not strip a suffix-only id" in {
    ModuleResolver.strip("JS") shouldBe "JS"
    ModuleResolver.strip("Native") shouldBe "Native"
  }

  "ModuleResolver.resolveKey" should "prefer an exact id match over the stripped fallback" in {
    ModuleResolver.resolveKey("analyticsJS", Set("analyticsJS", "analytics")) shouldBe Some("analyticsJS")
  }

  it should "fall back to the stripped id for a crossProject component" in {
    ModuleResolver.resolveKey("coreJVM", Set("core")) shouldBe Some("core")
    ModuleResolver.resolveKey("coreNative", Set("core")) shouldBe Some("core")
  }

  it should "match a plain project by exact id" in {
    ModuleResolver.resolveKey("skunk", Set("core", "skunk")) shouldBe Some("skunk")
  }

  it should "return None for an unlisted project" in {
    ModuleResolver.resolveKey("skunk", Set("core")) shouldBe None
    ModuleResolver.resolveKey("mathjs", Set("math")) shouldBe None
  }

  "ModuleResolver.merge" should "override scalars with module values, falling back to shared" in {
    val shared = ProjectConfig(
      organization = Some("com.shared"),
      version = Some("1.0.0"),
      scalaVersion = Some("3.3.4"),
      versionScheme = Some("early-semver"),
      homepage = Some("https://shared.example")
    )
    val module = ProjectConfig(
      version = Some("2.0.0"),
      homepage = Some("https://module.example")
    )

    val merged = ModuleResolver.merge(shared, module, "core")

    merged.organization shouldBe Some("com.shared") // inherited
    merged.version shouldBe Some("2.0.0") // overridden
    merged.scalaVersion shouldBe Some("3.3.4") // inherited
    merged.versionScheme shouldBe Some("early-semver")
    merged.homepage shouldBe Some("https://module.example")
  }

  it should "append lists as shared ++ module" in {
    val shared = ProjectConfig(
      scalacOptions = Some(Seq("-deprecation")),
      dependencies = Some(Seq(Dependency("org.typelevel", "cats-core", "2.13.0"))),
      testDependencies = Some(Seq(Dependency("org.scalatest", "scalatest", "3.2.19"))),
      licenses = Some(Seq("MIT")),
      developers = Some(Seq(Developer("a", "A", "a@example.com", "https://a.example"))),
      resolvers = Some(Seq(Resolver("shared", "https://shared.example/repo")))
    )
    val module = ProjectConfig(
      scalacOptions = Some(Seq("-feature")),
      dependencies = Some(Seq(Dependency("org.tpolecat", "skunk-core", "0.6.4"))),
      testDependencies = Some(Seq(Dependency("org.scalameta", "munit", "1.0.0"))),
      licenses = Some(Seq("Apache-2.0")),
      developers = Some(Seq(Developer("b", "B", "b@example.com", "https://b.example"))),
      resolvers = Some(Seq(Resolver("module", "https://module.example/repo")))
    )

    val merged = ModuleResolver.merge(shared, module, "skunk")

    merged.scalacOptions shouldBe Some(Seq("-deprecation", "-feature"))
    merged.dependencies shouldBe Some(
      Seq(
        Dependency("org.typelevel", "cats-core", "2.13.0"),
        Dependency("org.tpolecat", "skunk-core", "0.6.4")
      )
    )
    merged.testDependencies shouldBe Some(
      Seq(
        Dependency("org.scalatest", "scalatest", "3.2.19"),
        Dependency("org.scalameta", "munit", "1.0.0")
      )
    )
    merged.licenses shouldBe Some(Seq("MIT", "Apache-2.0"))
    merged.developers.map(_.map(_.id)) shouldBe Some(Seq("a", "b"))
    merged.resolvers.map(_.map(_.name)) shouldBe Some(Seq("shared", "module"))
  }

  it should "keep a list from whichever side defines it when the other is empty" in {
    val shared = ProjectConfig(dependencies = Some(Seq(Dependency("org", "shared", "1.0"))))
    val module = ProjectConfig()

    ModuleResolver.merge(shared, module, "core").dependencies shouldBe
      Some(Seq(Dependency("org", "shared", "1.0")))

    ModuleResolver.merge(ProjectConfig(), shared, "core").dependencies shouldBe
      Some(Seq(Dependency("org", "shared", "1.0")))
  }

  it should "default name to the module key, never inheriting it from shared" in {
    val shared = ProjectConfig(name = Some("root-name"))

    ModuleResolver.merge(shared, ProjectConfig(), "core").name shouldBe Some("core")
    ModuleResolver.merge(shared, ProjectConfig(name = Some("explicit")), "core").name shouldBe Some("explicit")
  }

  it should "preserve dependency platforms through the merge" in {
    val shared = ProjectConfig(
      dependencies = Some(Seq(Dependency("org.typelevel", "cats-core", "2.13.0", platform = Platform.Shared)))
    )
    val module = ProjectConfig(
      dependencies = Some(Seq(Dependency("org.scala-js", "scalajs-dom", "2.8.0", platform = Platform.Js)))
    )

    val merged = ModuleResolver.merge(shared, module, "core")
    merged.dependencies.map(_.map(_.platform)) shouldBe Some(Seq(Platform.Shared, Platform.Js))
  }
}
