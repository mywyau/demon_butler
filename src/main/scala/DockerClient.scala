//import cats.effect.Sync

import cats.effect.std.Console
import cats.effect.syntax.spawn._
import cats.effect.{Async, Concurrent}
import cats.syntax.all.*
import configuration.models.*
import fs2.concurrent.SignallingRef
import fs2.io.process.{ProcessBuilder, Processes, *}
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.*


trait DockerClientAlgebra[F[_]] {

  def runDockerComposeCommand(serviceConfig: ServiceConfig): F[Unit]

  //  def stopContainer(containerName: String): F[Unit]
  //
  //  def removeContainer(containerName: String): F[Unit]
  //
  //  def listContainers(): F[String]
}

class DockerClientImpl[F[_] : Async : Concurrent : Logger : Console : Processes](config: AppConfig) extends DockerClientAlgebra[F] {

  private def determineBasePath(serviceConfig: ServiceConfig): String =
    if (serviceConfig.name.contains("frontend")) config.frontendBasePath else config.backendBasePath

  private def buildCommandArgs(serviceConfig: ServiceConfig, projectPath: String): List[String] = {
    val commandParts = serviceConfig.command.split(" ").toList
    if (commandParts.isEmpty) throw new IllegalArgumentException("Command is empty in service configuration")
    List("-f", s"$projectPath/${serviceConfig.fileName}") ++ commandParts.tail
  }

  override def runDockerComposeCommand(serviceConfig: ServiceConfig): F[Unit] = {
    val basePath: String = determineBasePath(serviceConfig)
    val projectPath = s"$basePath${serviceConfig.projectPath}"
    val commandParts = serviceConfig.command.split(" ").toList

    if (commandParts.isEmpty) {
      Async[F].raiseError(new IllegalArgumentException("Command is empty in service configuration"))
    } else {
      val baseCommand = commandParts.head
      val args = List("-f", s"$projectPath/${serviceConfig.fileName}") ++ commandParts.tail

      Logger[F].info(s"Running command: $baseCommand ${args.mkString(" ")}") *>
        ProcessBuilder(baseCommand, args: _*)
          .spawn[F]
          .use { process =>
            for {
              // Create a SignallingRef to control the progress indicator
              isRunning <- SignallingRef[F, Boolean](true)

              // Stream to track stdout
              stdoutStream =
                process.stdout
                  .through(fs2.text.utf8.decode)
                  .through(fs2.text.lines)

              // Progress indicator tied to the SignallingRef
              progressIndicator =
                fs2.Stream
                  .awakeEvery[F](1.second)
                  .as(s"[${serviceConfig.name}] - Building containers...") // Ensure it emits String
                  //                .evalMap(_ => Logger[F].info(s"[${serviceConfig.name}] - Building containers..."))
                  .interruptWhen(isRunning.discrete.filter(!_))

              // Combined stream of stdout and progress indicator
              combinedStream =
                stdoutStream
                  .merge(progressIndicator)
                  .evalMap(line => Logger[F].info(line))

              // Run the combined stream while capturing the process exit value
              _ <- combinedStream.compile.drain.start

              exitCode <- process.exitValue

              // Signal the progress indicator to stop
              _ <- isRunning.set(false)

              // Handle the exit code
              _ <- if (exitCode == 0) {
                Logger[F].info(s"Successfully ran docker compose for service: ${serviceConfig.name}")
              } else {
                Logger[F].error(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $exitCode") *>
                  Async[F].raiseError(new RuntimeException(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $exitCode"))
              }
            } yield ()
          }
    }
  }


  //  override def runDockerComposeCommand(serviceConfig: ServiceConfig): F[Unit] = {
  //    val basePath = if (serviceConfig.name.contains("frontend")) config.frontendBasePath else config.backendBasePath
  //    val projectPath = s"$basePath${serviceConfig.projectPath}"
  //
  //    val commandParts = serviceConfig.command.split(" ").toList
  //
  //    if (commandParts.isEmpty) {
  //      Async[F].raiseError(new IllegalArgumentException("Command is empty in service configuration"))
  //    } else {
  //      val baseCommand = commandParts.head
  //      val args = List("-f", s"$projectPath/${serviceConfig.fileName}") ++ commandParts.tail
  //
  //      Logger[F].info(s"Running command: $baseCommand ${args.mkString(" ")}") *>
  //        ProcessBuilder(baseCommand, args: _*)
  //          .spawn[F]
  //          .use { process =>
  //            val stdoutStream = process.stdout.through(fs2.text.utf8.decode).through(fs2.text.lines)
  //
  //            val progressIndicator =
  //              fs2.Stream
  //                .awakeEvery[F](1.second)
  //                .as(s"[${serviceConfig.name}] - Building containers...") // Transform progress updates to Strings
  //
  //            // Combine the process output with progress updates
  //            stdoutStream
  //              .merge(progressIndicator)
  //              .evalMap(line => Logger[F].info(line))
  //              .compile
  //              .drain *>
  //              process.exitValue.flatMap { code =>
  //                if (code == 0) {
  //                  Logger[F].info(s"Successfully ran docker compose for service: ${serviceConfig.name}")
  //                } else {
  //                  Logger[F].error(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $code") *>
  //                    Async[F].raiseError(new RuntimeException(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $code"))
  //                }
  //              }
  //          }
  //    }
  //  }

}

object DockerClient {
  def apply[F[_] : Async : Logger : Console : Processes](config: AppConfig): DockerClientAlgebra[F] =
    new DockerClientImpl[F](config)
}
