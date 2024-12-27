def extractHostPath(): String = {
  val userHome = System.getProperty("user.home")
  val workingDir = System.getProperty("user.dir")

  s"User Home: $userHome, Current Working Directory: $workingDir"
}

println(extractHostPath())