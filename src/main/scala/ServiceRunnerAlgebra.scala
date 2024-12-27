import cats.effect.Sync
import configuration.models.{AppConfig, ServiceConfig}
import org.typelevel.log4cats.Logger

trait ServiceRunnerAlgebra[F[_]] {
  
  def runDockerCompose(service: ServiceConfig): F[Unit]
  
}

class ServiceRunnerImpl[F[_] : Sync : Logger](docker: DockerClientAlgebra[F], config: AppConfig)
  extends ServiceRunnerAlgebra[F] {

  override def runDockerCompose(service: ServiceConfig): F[Unit] =
    docker.startAllFrontendContainers(service)
}