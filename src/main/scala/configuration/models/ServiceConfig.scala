package configuration.models

import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader

//name = "wander"
//container - name = "wander-container"
//image = "wander-frontend"
//ports = [
//"3000:3000"
//]
//project - path = "/wander"
//file - name = "docker-compose.yml"
//command = "docker-compose up nextjs-dev --build"

case class ServiceConfig(
                          name: String,
                          containerName: String,
                          image: String,
                          ports: List[String],
                          projectPath: String,
                          fileName: String,
                          command: String,
                        )derives ConfigReader

