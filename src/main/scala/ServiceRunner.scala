import cats.effect.*
import cats.syntax.all.*
import configuration.models.{AppConfig, ServiceConfig}
import io.circe.generic.auto.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

trait ServiceRunnerAlgebra[F[_]] {


  def runDockerCompose(serviceName: String): F[Unit]

  def buildAndRunService(service: ServiceConfig): F[Unit]

  def stopAndRemoveService(service: ServiceConfig): F[Unit]

  def listServices: F[Unit]
}

class ServiceRunner[F[_] : Sync : Logger](docker: DockerClientAlgebra[F], config: AppConfig)
  extends ServiceRunnerAlgebra[F] {

  override def runDockerCompose(): F[Unit] =
    docker.runDev(build = true)

  override def buildAndRunService(service: ServiceConfig): F[Unit] =
    for {
      _ <- Logger[F].info(s"Building and running service: ${service.name}")
      _ <- docker.buildImage(service.image, service.path)
        .handleErrorWith { ex =>
          Logger[F].error(ex)(s"Failed to build image for service ${service.name}")
        }
      _ <- docker.createContainer(service.image, service.containerName, service.ports)
        .handleErrorWith { ex =>
          Logger[F].error(ex)(s"Failed to create container for service ${service.name}")
        }
      _ <- docker.startContainer(service.containerName)
        .handleErrorWith { ex =>
          Logger[F].error(ex)(s"Failed to start container for service ${service.name}")
        }
      _ <- Logger[F].info(s"Service ${service.name} is now running.")
    } yield ()

  override def stopAndRemoveService(service: ServiceConfig): F[Unit] =
    for {
      _ <- Logger[F].info(s"Stopping and removing service: ${service.name}")
      _ <- docker.stopContainer(service.containerName)
        .handleErrorWith { ex =>
          Logger[F].error(ex)(s"Failed to stop container for service ${service.name}")
        }
      _ <- docker.removeContainer(service.containerName)
        .handleErrorWith { ex =>
          Logger[F].error(ex)(s"Failed to remove container for service ${service.name}")
        }
      _ <- Logger[F].info(s"Service ${service.name} has been stopped and removed.")
    } yield ()

  override def listServices: F[Unit] =
    for {
      _ <- Logger[F].info("Listing all containers:")
      containers <- docker.listContainers()
      _ <- Logger[F].info(containers)
    } yield ()
}

object ServiceRunnerApp extends IOApp {

  private def loadConfig[F[_] : Sync : Logger]: F[AppConfig] =
    Sync[F].delay(ConfigSource.default.load[AppConfig]).flatMap {
      case Left(failures) =>
        val errorMsg = s"Configuration loading failed: ${failures.toList.mkString(", ")}"
        Logger[F].error(errorMsg) *> Sync[F].raiseError(new RuntimeException(errorMsg))
      case Right(config) => Sync[F].pure(config)
    }

  private def createDockerClient[F[_] : Sync : Logger]: F[DockerClientAlgebra[F]] =
    Sync[F].delay(new DockerClientImpl[F])

  private def createServiceRunner[F[_] : Sync : Logger](docker: DockerClientAlgebra[F], config: AppConfig): F[ServiceRunnerAlgebra[F]] =
    Sync[F].pure(new ServiceRunner[F](docker, config))

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

    val program =
      for {
        config <- loadConfig[IO]
        docker <- createDockerClient[IO]
        serviceRunner <- createServiceRunner(docker, config)

        exitCode <- args match {
          case "start dev" :: Nil =>
            serviceRunner.runDockerCompose.as(ExitCode.Success)

          case "start" :: serviceName :: Nil =>
            config.services.find(_.name == serviceName) match {
              case Some(service) => serviceRunner.buildAndRunService(service).as(ExitCode.Success)
              case None =>
                Logger[IO].warn(s"Service $serviceName not found") *> IO.pure(ExitCode.Error)
            }

          case "stop" :: serviceName :: Nil =>
            config.services.find(_.name == serviceName) match {
              case Some(service) => serviceRunner.stopAndRemoveService(service).as(ExitCode.Success)
              case None =>
                Logger[IO].warn(s"Service $serviceName not found") *> IO.pure(ExitCode.Error)
            }

          case "list" :: Nil =>
            serviceRunner.listServices.as(ExitCode.Success)

          case _ =>
            Logger[IO].error(s"Invalid command: ${args.mkString(" ")}. Usage: start <serviceName> | stop <serviceName> | list") *>
              IO.pure(ExitCode.Error)
        }
      } yield exitCode

    program.handleErrorWith { ex =>
      Logger[IO].error(ex)("An unexpected error occurred") *> IO.pure(ExitCode.Error)
    }
  }
}
