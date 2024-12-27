package configuration.models

import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader

case class ServiceConfig(
                          name: String,
                          containerName: String,
                          image: String,
                          ports: List[String],
                          path: String // Directory path of the service
                        ) derives ConfigReader

//object ServiceConfig {
//
//  implicit val availabilityEncoder: Encoder[ServiceConfig] = deriveEncoder[ServiceConfig]
//}

