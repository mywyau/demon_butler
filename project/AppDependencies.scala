import sbt.*

object AppDependencies {

  // Define versions for libraries
  val catsCoreVersion = "2.10.0"
  val catsEffectVersion = "3.5.7"
  val http4sVersion = "0.23.28"
  val circeVersion = "0.14.7"
  val weaverVersion = "0.8.3"

  // Compile dependencies
  val compile: Seq[ModuleID] = Seq(
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
    "ch.qos.logback" % "logback-classic" % "1.5.6" exclude("org.slf4j", "slf4j-jdk14"),
    "org.typelevel" %% "cats-core" % catsCoreVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "co.fs2" %% "fs2-core" % "3.8.0",
    "co.fs2" %% "fs2-io" % "3.8.0",
    "com.github.pureconfig" %% "pureconfig-core" % "0.17.8"
  )

  // Test dependencies
  val test: Seq[ModuleID] = Seq(
    "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
    "com.disneystreaming" %% "weaver-scalacheck" % "0.7.6" % Test
  )

  // Integration test dependencies
  val integrationTest: Seq[ModuleID] = Seq(
    "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
    "org.http4s" %% "http4s-ember-client" % "0.23.28" % Test,
    "com.disneystreaming" %% "weaver-scalacheck" % "0.7.6" % Test,
    "com.github.pureconfig" %% "pureconfig-core" % "0.17.8" % Test
  )

  // Additional workaround for macOS if needed
  def macOsWorkaround(): Seq[ModuleID] =
    if (sys.props.get("os.name").exists(_.toLowerCase.contains("mac"))) {
      Seq("org.reactivemongo" % "reactivemongo-shaded-native" % "0.20.3-osx-x86-64" % Test)
    } else Seq()

  // Aggregate all dependencies
  def apply(): Seq[ModuleID] = compile ++ test ++ integrationTest ++ macOsWorkaround()
}
