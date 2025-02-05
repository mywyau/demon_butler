package configuration.models

import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader

case class AppConfig(
                      frontendBasePath: String,
                      backendBasePath: String,
                      services: List[ServiceConfig]
                    ) derives ConfigReader
