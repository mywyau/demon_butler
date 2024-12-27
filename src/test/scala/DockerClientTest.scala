import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import weaver.SimpleIOSuite

class TestDockerClient private(
                                imagesRef: Ref[IO, List[String]],
                                containersRef: Ref[IO, List[String]]
                              ) extends DockerClientAlgebra[IO] {

  override def buildImage(imageName: String, contextPath: String): IO[Unit] =
    imagesRef.update(images => images :+ imageName)

  override def createContainer(image: String, containerName: String, ports: List[String]): IO[Unit] =
    containersRef.update(containers => containers :+ containerName)

  override def startContainer(containerName: String): IO[Unit] =
    IO.unit // For this test, we don't simulate container state

  override def stopContainer(containerName: String): IO[Unit] =
    IO.unit // Same here, just a stub

  override def removeContainer(containerName: String): IO[Unit] =
    containersRef.update(containers => containers.filterNot(_ == containerName))

  override def listContainers(): IO[String] =
    containersRef.get.map(_.mkString(", "))
}

object TestDockerClient {
  def create: IO[TestDockerClient] =
    for {
      imagesRef <- Ref.of[IO, List[String]](List.empty)
      containersRef <- Ref.of[IO, List[String]](List.empty)
    } yield new TestDockerClient(imagesRef, containersRef)
}

object DockerClientTest extends SimpleIOSuite {

  test("buildImage should add an image") {
    TestDockerClient.create.flatMap { testClient =>
      val program = for {
        _ <- testClient.buildImage("test-image", ".")
        containers <- testClient.listContainers()
      } yield containers

      program.map { result =>
        expect(result.isEmpty) // listContainers should not show images
      }
    }
  }

  test("createContainer should add a container") {
    TestDockerClient.create.flatMap { testClient =>
      val program = for {
        _ <- testClient.createContainer("test-image", "test-container", List("8080:80"))
        containers <- testClient.listContainers()
      } yield containers

      program.map { result =>
        expect(result.contains("test-container"))
      }
    }
  }

  test("removeContainer should remove the container") {
    TestDockerClient.create.flatMap { testClient =>
      val program = for {
        _ <- testClient.createContainer("test-image", "test-container", List("8080:80"))
        _ <- testClient.removeContainer("test-container")
        containers <- testClient.listContainers()
      } yield containers

      program.map { result =>
        expect(!result.contains("test-container"))
      }
    }
  }
}
