import java.nio.file.{Files, Paths}
import scala.jdk.StreamConverters._

class PathCalculationService {

  // TODO: Impove path deduction capabilities

  def extractHostPath(): String = {
    val userHome = System.getProperty("user.home")
    val workingDir = System.getProperty("user.dir")

    s"User Home: $userHome, Current Working Directory: $workingDir"
  }


  def findPath(targetName: String, rootPath: String): Option[String] = {
    Files.walk(Paths.get(rootPath)).toScala(LazyList)
      .find(path => path.getFileName.toString == targetName)
      .map(_.toString)
  }

  // Example usage:
  val servicePath = findPath("frontend", "/Users/michaelyau/desk_booking")
  servicePath.foreach(println)

}
