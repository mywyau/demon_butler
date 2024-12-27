package configuration

import configuration.models.*

object AppConfigConstants {

  val frontend1ServiceConfig =
    ServiceConfig(
      name = "frontend1",
      containerName = "frontend-container-1",
      image = "your-frontend-service-1",
      ports = List("3000:3000"),
      path = "/path/to/frontend1"
    )

  val frontend2ServiceConfig =
    ServiceConfig(
      name = "frontend2",
      containerName = "frontend-container-2",
      image = "your-frontend-service-2",
      ports = List("3001:3001"),
      path = "/path/to/frontend2"
    )

  val appConfig =
    AppConfig(
      services = List(frontend1ServiceConfig, frontend2ServiceConfig)
    )

}
