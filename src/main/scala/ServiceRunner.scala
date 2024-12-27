import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.syntax.all.*
import configuration.models.{AppConfig, ServiceConfig}
import io.circe.generic.auto.*
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ConfigReader, ConfigSource}

trait ServiceRunnerAlgebra[F[_]] {

  def buildAndRunService(service: ServiceConfig): F[Unit]

  def stopAndRemoveService(service: ServiceConfig): F[Unit]

  def listServices: F[Unit]
}

class ServiceRunner[F[_] : Sync](docker: DockerClientAlgebra[F], config: AppConfig) extends ServiceRunnerAlgebra[F] {

  override def buildAndRunService(service: ServiceConfig): F[Unit] =
    for {
      _ <- Sync[F].delay(println(s"Building and running service: ${service.name}"))
      _ <- docker.buildImage(service.image, service.path)
      _ <- docker.createContainer(service.image, service.containerName, service.ports)
      _ <- docker.startContainer(service.containerName)
      _ <- Sync[F].delay(println(s"Service ${service.name} is running."))
    } yield ()

  override def stopAndRemoveService(service: ServiceConfig): F[Unit] =
    for {
      _ <- Sync[F].delay(println(s"Stopping and removing service: ${service.name}"))
      _ <- docker.stopContainer(service.containerName)
      _ <- docker.removeContainer(service.containerName)
      _ <- Sync[F].delay(println(s"Service ${service.name} has been stopped and removed."))
    } yield ()

  override def listServices: F[Unit] =
    for {
      _ <- Sync[F].delay(println("Listing all containers:"))
      containers <- docker.listContainers()
      _ <- Sync[F].delay(println(containers))
    } yield ()
}

object ServiceRunnerApp extends IOApp {

  private def loadConfig[F[_] : Sync]: F[AppConfig] =
    Sync[F].fromEither(
      ConfigSource.default.load[AppConfig]
        .leftMap(failures => new RuntimeException(s"Failed to load configuration: ${failures.toList.mkString(", ")}"))
    )

  override def run(args: List[String]): IO[ExitCode] = {
    val program =
      for {
        config <- loadConfig[IO]
        dockerClient = new DockerClientImpl[IO]
        serviceRunner = new ServiceRunner[IO](dockerClient, config)
        exitCode <- args match {
          case "start" :: serviceName :: Nil =>
            config.services.find(_.name == serviceName) match {
              case Some(service) => serviceRunner.buildAndRunService(service).as(ExitCode.Success)
              case None => IO(println(s"Service $serviceName not found")).as(ExitCode.Error)
            }

          case "stop" :: serviceName :: Nil =>
            config.services.find(_.name == serviceName) match {
              case Some(service) => serviceRunner.stopAndRemoveService(service).as(ExitCode.Success)
              case None => IO(println(s"Service $serviceName not found")).as(ExitCode.Error)
            }

          case "list" :: Nil =>
            serviceRunner.listServices.as(ExitCode.Success)

          case _ =>
            IO(println("Usage: start <serviceName> | stop <serviceName> | list")).as(ExitCode.Error)
        }
      } yield exitCode

    program.handleErrorWith { ex =>
      IO(println(s"Unexpected error occurred: ${ex.getMessage}")).as(ExitCode.Error)
    }
  }
}
