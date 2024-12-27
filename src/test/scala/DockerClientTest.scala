import cats.effect.IO
import cats.effect.unsafe.implicits.global
import weaver.SimpleIOSuite

class TestDockerClient extends DockerClientAlgebra[IO] {

  private var containers: List[String] = List.empty
  private var images: List[String] = List.empty

  override def buildImage(imageName: String, contextPath: String): IO[Unit] =
    IO(images = images :+ imageName)

  override def createContainer(image: String, containerName: String, ports: List[String]): IO[Unit] =
    IO(containers = containers :+ containerName)

  override def startContainer(containerName: String): IO[Unit] =
    IO.unit

  override def stopContainer(containerName: String): IO[Unit] =
    IO.unit

  override def removeContainer(containerName: String): IO[Unit] =
    IO(containers = containers.filterNot(_ == containerName))

  override def listContainers(): IO[String] =
    IO.pure(containers.mkString(", "))
}

object DockerClientTest extends SimpleIOSuite {
  val testClient = new TestDockerClient

  test("buildImage should add an image") {
    val program = for {
      _ <- testClient.buildImage("test-image", ".")
      containers <- testClient.listContainers()
    } yield containers

    program.map { result =>
      expect(result.isEmpty) // listContainers should not show images
    }
  }

  test("createContainer should add a container") {
    val program = for {
      _ <- testClient.createContainer("test-image", "test-container", List("8080:80"))
      containers <- testClient.listContainers()
    } yield containers

    program.map { result =>
      expect(result.contains("test-container"))
    }
  }

  test("removeContainer should remove the container") {
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
