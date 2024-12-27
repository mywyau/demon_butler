package configuration

import configuration.models.*

object AppConfigConstants {

  val frontend1ServiceConfig =
    ServiceConfig(
      name = "wander",
      containerName = "wander-container",
      image = "wander-frontend",
      ports = List("3000:3000"),
      path = "/Users/michaelyau/desk_booking/frontend/wander"
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
