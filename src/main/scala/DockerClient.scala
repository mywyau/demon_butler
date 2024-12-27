//import cats.effect.Sync

import cats.effect.Async
import cats.effect.std.Console
import cats.syntax.all.*
import configuration.models.*
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

// desired command    "docker-compose", "-f", s"$contextPath/docker-compose.yml", "up", serviceName

class DockerClientImpl[F[_] : Async : Logger : Console : Processes](config: AppConfig) extends DockerClientAlgebra[F] {

  override def runDockerComposeCommand(serviceConfig: ServiceConfig): F[Unit] = {
    val basePath = if (serviceConfig.name.contains("frontend")) config.frontendBasePath else config.backendBasePath
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
            val stdoutStream = process.stdout.through(fs2.text.utf8.decode).through(fs2.text.lines)

            val progressIndicator =
              fs2.Stream
                .awakeEvery[F](1.second)
                .as(s"[${serviceConfig.name}] - Building containers...") // Transform progress updates to Strings

            // Combine the process output with progress updates
            stdoutStream
              .merge(progressIndicator)
              .evalMap(line => Logger[F].info(line))
              .compile
              .drain *>
              process.exitValue.flatMap { code =>
                if (code == 0) {
                  Logger[F].info(s"Successfully ran docker compose for service: ${serviceConfig.name}")
                } else {
                  Logger[F].error(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $code") *>
                    Async[F].raiseError(new RuntimeException(s"Failed to run docker compose for service: ${serviceConfig.name} with exit code $code"))
                }
              }
          }
    }
  }

}

object DockerClient {
  def apply[F[_] : Async : Logger : Console : Processes](config: AppConfig): DockerClientAlgebra[F] =
    new DockerClientImpl[F](config)
}
