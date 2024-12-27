
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import configuration.models.{AppConfig, ServiceConfig}
import fs2.io.process.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

object ServiceRunnerApp extends IOApp {

  private def loadConfig[F[_] : Sync : Logger]: F[AppConfig] =
    Sync[F].delay(ConfigSource.default.load[AppConfig]).flatMap {
      case Left(failures) =>
        val errorMsg = s"Configuration loading failed: ${failures.toList.mkString(", ")}"
        Logger[F].error(errorMsg) *> Sync[F].raiseError(new RuntimeException(errorMsg))
      case Right(config) => Sync[F].pure(config)
    }

  private def createDockerClient[F[_] : Async : Concurrent : Logger : Console : Processes](config: AppConfig): DockerClientImpl[F] =
    new DockerClientImpl[F](config)

  private def createServiceRunner[F[_] : Async : Concurrent : Logger : Console : Processes](docker: DockerClientAlgebra[F], config: AppConfig): ServiceRunnerImpl[F] =
    new ServiceRunnerImpl[F](docker, config)

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

    val program =
      for {
        config <- loadConfig[IO]
        docker = createDockerClient[IO](config)
        serviceRunner = createServiceRunner(docker, config)

        exitCode <- args match {
          case "startDev" :: Nil =>
            config.services.parTraverse(serviceRunner.runDockerCompose).as(ExitCode.Success)
          case "list" :: Nil =>
            docker
              .listContainers()
              .as(ExitCode.Success)
          case "stopAll" :: Nil =>
            docker
              .stopAllContainers()
              .as(ExitCode.Success)
          case _ =>
            Logger[IO].error(s"Invalid command: ${args.mkString(" ")}. Usage: startDev | start <serviceName>") *>
              IO.pure(ExitCode.Error)
        }
      } yield exitCode

    program.handleErrorWith { ex =>
      Logger[IO].error(ex)("An unexpected error occurred") *> IO.pure(ExitCode.Error)
    }
  }
}
