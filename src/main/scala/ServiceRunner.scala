import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.*
import configuration.models.{AppConfig, ServiceConfig}
import pureconfig.ConfigSource

object ServiceRunner extends IOApp {

  def buildAndRunService(docker: DockerClient, service: ServiceConfig): IO[Unit] =
    IO {
      println(s"Building and running service: ${service.name}")
      docker.buildImage(service.path, service.image)
      docker.createContainer(service.image, service.containerName, service.ports)
      docker.startContainer(service.containerName)
      println(s"Service ${service.name} is running.")
    }

  def stopAndRemoveService(docker: DockerClient, service: ServiceConfig): IO[Unit] =
    IO {
      println(s"Stopping and removing service: ${service.name}")
      docker.stopContainer(service.containerName)
      docker.removeContainer(service.containerName)
      println(s"Service ${service.name} has been stopped and removed.")
    }

  def listServices(docker: DockerClient): IO[Unit] =
    IO {
      println("Listing all containers:")
      println(docker.listContainers())
    }

  override def run(args: List[String]): IO[ExitCode] = {

    val docker = new DockerClient()

    // Load configuration
    val config = ConfigSource.default.loadOrThrow[AppConfig]

    // Parse command-line arguments
    args match {
      case "start" :: serviceName :: Nil =>
        config.services.find(_.name == serviceName) match {
          case Some(service) => buildAndRunService(docker, service).as(ExitCode.Success)
          case None => IO(println(s"Service $serviceName not found")).as(ExitCode.Error)
        }

      case "stop" :: serviceName :: Nil =>
        config.services.find(_.name == serviceName) match {
          case Some(service) => stopAndRemoveService(docker, service).as(ExitCode.Success)
          case None => IO(println(s"Service $serviceName not found")).as(ExitCode.Error)
        }

      case "list" :: Nil =>
        listServices(docker).as(ExitCode.Success)

      case _ =>
        IO(println("Usage: start <serviceName> | stop <serviceName> | list")).as(ExitCode.Error)
    }
  }
}
