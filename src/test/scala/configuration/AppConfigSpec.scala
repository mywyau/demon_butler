package configuration

import cats.effect.IO
import configuration.AppConfigConstants.*
import weaver.SimpleIOSuite

object AppConfigSpec extends SimpleIOSuite {

  val configReader: ConfigReaderAlgebra[IO] = ConfigReader[IO]

  test("load test configurations correctly") {

    for {
      config <- configReader.loadAppConfig
    } yield {
      expect.all(
        config == appConfig,
        config.services == appConfig.services,
      )
    }
  }
}
