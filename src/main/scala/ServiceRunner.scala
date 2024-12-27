
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import configuration.models.{AppConfig, ServiceConfig}
import fs2.io.process.*
import io.circe.generic.auto.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

trait ServiceRunnerAlgebra[F[_]] {
  def runDockerCompose(service: ServiceConfig): F[Unit]
}

class ServiceRunner[F[_] : Sync : Logger](docker: DockerClientAlgebra[F], config: AppConfig)
  extends ServiceRunnerAlgebra[F] {

  override def runDockerCompose(service: ServiceConfig): F[Unit] =
    docker.runDockerComposeCommand(service)
}

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

  private def createServiceRunner[F[_] : Async : Concurrent : Logger : Console : Processes](docker: DockerClientAlgebra[F], config: AppConfig): ServiceRunner[F] =
    new ServiceRunner[F](docker, config)

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

          case "start" :: serviceName :: Nil =>
            config.services.find(_.name == serviceName) match {
              case Some(service) =>
                serviceRunner.runDockerCompose(service).as(ExitCode.Success)
              case None =>
                Logger[IO].warn(s"Service $serviceName not found") *> IO.pure(ExitCode.Error)
            }

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
