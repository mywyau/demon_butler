import scala.sys.process._

class DockerClient {

  def buildImage(imageName: String, contextPath: String): Int = {
    val command = Seq("docker", "build", "-t", imageName, contextPath)
    println(s"Running command: ${command.mkString(" ")}")
    command.!
  }

  def createContainer(image: String, containerName: String, ports: List[String]): Unit = {
    val portArgs = ports.flatMap(port => Seq("-p", port))
    val cmd = Seq("docker", "create", "--name", containerName) ++ portArgs ++ Seq(image)

    if (cmd.! != 0) throw new RuntimeException(s"Failed to create container $containerName")
  }

  def startContainer(containerName: String): Unit = {
    if (Seq("docker", "start", containerName).! != 0) {
      throw new RuntimeException(s"Failed to start container $containerName")
    }
  }

  def stopContainer(containerName: String): Unit = {
    if (Seq("docker", "stop", containerName).! != 0) {
      throw new RuntimeException(s"Failed to stop container $containerName")
    }
  }

  def removeContainer(containerName: String): Unit = {
    if (Seq("docker", "rm", containerName).! != 0) {
      throw new RuntimeException(s"Failed to remove container $containerName")
    }
  }

  def listContainers(): String = {
    Seq("docker", "ps", "-a").!!
  }
}
