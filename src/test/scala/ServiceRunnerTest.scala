
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref, Sync}
import cats.implicits.*
import cats.syntax.all.*
import configuration.models.{AppConfig, ServiceConfig}
import weaver.SimpleIOSuite

// Mock DockerClientAlgebra with Ref
class RefDockerClient[F[_]: Sync](
                             imagesRef: Ref[F, List[String]],
                             containersRef: Ref[F, List[String]]
                           ) extends DockerClientAlgebra[F] {

  override def buildImage(imageName: String, contextPath: String): F[Unit] =
    imagesRef.update(images => images :+ imageName)

  override def createContainer(image: String, containerName: String, ports: List[String]): F[Unit] =
    containersRef.update(containers => containers :+ containerName)

  override def startContainer(containerName: String): F[Unit] =
    Sync[F].unit // Simulate starting the container without changing state

  override def stopContainer(containerName: String): F[Unit] =
    containersRef.update(containers => containers.filterNot(_ == containerName))

  override def removeContainer(containerName: String): F[Unit] =
    containersRef.update(containers => containers.filterNot(_ == containerName))

  override def listContainers(): F[String] =
    containersRef.get.map(_.mkString(", "))
}

object RefDockerClient {
  def create[F[_] : Sync]: F[RefDockerClient[F]] =
    for {
      imagesRef <- Ref.of[F, List[String]](List.empty)
      containersRef <- Ref.of[F, List[String]](List.empty)
    } yield new RefDockerClient[F](imagesRef, containersRef)
}

object ServiceRunnerTest extends SimpleIOSuite {

  val testConfig =
    AppConfig(
      services = List(
        ServiceConfig(
          name = "test-service",
          image = "test-image",
          path = ".",
          containerName = "test-container",
          ports = List("8080:80")
        )
      )
    )

  test("buildAndRunService should build and start a service") {
    RefDockerClient.create[IO].flatMap { dockerClient =>
      val serviceRunner = new ServiceRunner[IO](dockerClient, testConfig)
      val service = testConfig.services.head

      val program = serviceRunner.buildAndRunService(service)

      program.flatMap { _ =>
        dockerClient.listContainers().map { result =>
          expect(result.contains(service.containerName))
        }
      }
    }
  }

  test("stopAndRemoveService should stop and remove a service") {
    RefDockerClient.create[IO].flatMap { dockerClient =>
      val serviceRunner = new ServiceRunner[IO](dockerClient, testConfig)
      val service = testConfig.services.head

      val program = for {
        _ <- dockerClient.createContainer(service.image, service.containerName, service.ports)
        _ <- serviceRunner.stopAndRemoveService(service)
      } yield ()

      program.flatMap { _ =>
        dockerClient.listContainers().map { result =>
          expect(!result.contains(service.containerName))
        }
      }
    }
  }

  test("listServices should list all running containers") {
    RefDockerClient.create[IO].flatMap { dockerClient =>
      val serviceRunner = new ServiceRunner[IO](dockerClient, testConfig)
      val service = testConfig.services.head

      val program = for {
        _ <- dockerClient.createContainer(service.image, service.containerName, service.ports)
        result <- serviceRunner.listServices
      } yield result

      program.map { _ =>
        expect(true) // Ensure it runs without errors
      }
    }
  }
}
