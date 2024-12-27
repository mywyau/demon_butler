ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / parallelExecution := true

lazy val root = (project in file("."))
  .settings(
    name := "demon_butler",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / run / fork := true,
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "scala"
  )

lazy val it = (project in file("it"))
  .dependsOn(root) // Depend on root
  .settings(
    name := "demon_butler-it",
    libraryDependencies ++= AppDependencies.integrationTest,
    fork := true,
    parallelExecution := true,
    scalaSource := baseDirectory.value / "src" / "test" / "scala",
    Test / unmanagedSourceDirectories += baseDirectory.value / "src" / "test" / "scala"
  )


// Merge strategy for sbt assembly for containerising the app
import sbtassembly.AssemblyPlugin.autoImport.*

assembly / assemblyMergeStrategy := {
    case PathList("META-INF", "services", "org.slf4j.spi.SLF4JServiceProvider") =>
        MergeStrategy.first // Ensure SLF4J can find its service provider

    case PathList("META-INF", "io.netty.versions.properties") =>
        MergeStrategy.first

    case PathList("module-info.class") =>
        MergeStrategy.discard

    case PathList("META-INF", xs @ _*) if xs.contains("MANIFEST.MF") =>
        MergeStrategy.discard // Discard additional META-INF files except for services

    case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
}


assembly / assemblyExcludedJars := {
    val cp = (assembly / fullClasspath).value
    cp.filter(_.data.getName.contains("-tests.jar"))
}

addCommandAlias("demon", "run")
