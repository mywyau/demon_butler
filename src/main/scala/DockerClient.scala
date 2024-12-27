import scala.sys.process._
import cats.effect.Sync
import cats.syntax.all._
import scala.sys.process._

trait DockerClientAlgebra[F[_]] {
  def buildImage(imageName: String, contextPath: String): F[Unit]
  def createContainer(image: String, containerName: String, ports: List[String]): F[Unit]
  def startContainer(containerName: String): F[Unit]
  def stopContainer(containerName: String): F[Unit]
  def removeContainer(containerName: String): F[Unit]
  def listContainers(): F[String]
}

class DockerClientImpl[F[_]: Sync] extends DockerClientAlgebra[F] {

  override def buildImage(imageName: String, contextPath: String): F[Unit] =
    Sync[F].delay {
      val command = Seq("docker", "build", "-t", imageName, contextPath)
      println(s"Running command: ${command.mkString(" ")}")
      if (command.! != 0) {
        throw new RuntimeException(s"Failed to build image $imageName")
      }
    }

  override def createContainer(image: String, containerName: String, ports: List[String]): F[Unit] =
    Sync[F].delay {
      val portArgs = ports.flatMap(port => Seq("-p", port))
      val cmd = Seq("docker", "create", "--name", containerName) ++ portArgs ++ Seq(image)
      println(s"Running command: ${cmd.mkString(" ")}")
      if (cmd.! != 0) {
        throw new RuntimeException(s"Failed to create container $containerName")
      }
    }

  override def startContainer(containerName: String): F[Unit] =
    Sync[F].delay {
      if (Seq("docker", "start", containerName).! != 0) {
        throw new RuntimeException(s"Failed to start container $containerName")
      }
    }

  override def stopContainer(containerName: String): F[Unit] =
    Sync[F].delay {
      if (Seq("docker", "stop", containerName).! != 0) {
        throw new RuntimeException(s"Failed to stop container $containerName")
      }
    }

  override def removeContainer(containerName: String): F[Unit] =
    Sync[F].delay {
      if (Seq("docker", "rm", containerName).! != 0) {
        throw new RuntimeException(s"Failed to remove container $containerName")
      }
    }

  override def listContainers(): F[String] =
    Sync[F].delay {
      Seq("docker", "ps", "-a").!!
    }
}
