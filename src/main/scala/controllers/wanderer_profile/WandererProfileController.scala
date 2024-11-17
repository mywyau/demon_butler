package controllers.wanderer_profile

import cats.effect.unsafe.implicits.global
import cats.data.Validated.{Invalid, Valid}
import cats.effect.{Concurrent, IO}
import cats.implicits.*
import io.circe.syntax.EncoderOps
import models.users.wanderer_profile.errors.*
import models.users.wanderer_profile.requests.{UpdateProfileRequest, UserSignUpRequest}
import models.users.wanderer_profile.responses.error.{ErrorResponse, WandererProfileErrorResponse}
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import services.wanderer_profile.WandererProfileServiceAlgebra


trait WandererProfileController[F[_]] {
  def routes: HttpRoutes[F]
}

class WandererProfileControllerImpl[F[_] : Concurrent](
                                                        wandererProfileService: WandererProfileServiceAlgebra[F]
                                                      ) extends Http4sDsl[F] with WandererProfileController[F] {

  implicit val userSignUpRequestDecoder: EntityDecoder[F, UserSignUpRequest] = jsonOf[F, UserSignUpRequest]
  implicit val updateProfileRequestDecoder: EntityDecoder[F, UpdateProfileRequest] = jsonOf[F, UpdateProfileRequest]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "wanderer" / "user" / "profile" / user_id =>
      wandererProfileService.createProfile(user_id).flatMap {
        case Valid(userProfile) =>
          Ok(userProfile.asJson)
        case Invalid(errors) =>

          val loginDetailsErrors: List[ErrorResponse] = errors.collect {
            case error: WandererLoginDetailsError => ErrorResponse(error.code, error.message)
          }

          val addressErrors: List[ErrorResponse] = errors.collect {
            case error: WandererAddressError => ErrorResponse(error.code, error.message)
          }

          val contactDetailsErrors: List[ErrorResponse] = errors.collect {
            case error: WandererPersonalDetailsError => ErrorResponse(error.code, error.message)
          }

          val otherWandererProfileError: List[ErrorResponse] = errors.collect {
            case error: OtherWandererProfileError => ErrorResponse(error.code, error.message)
          }

          val errorResponse: WandererProfileErrorResponse =
            WandererProfileErrorResponse(
              loginDetailsErrors = loginDetailsErrors,
              addressErrors = addressErrors,
              contactDetailsErrors = contactDetailsErrors,
              otherErrors = otherWandererProfileError
            )

          BadRequest(errorResponse.asJson)
      }


    case req@PUT -> Root / "wanderer" / "user" / "profile" / userId =>
      IO(println(s"Received a PUT /wanderer/user/profile/$userId request")).unsafeRunSync() // Print immediately
      req.decode[UpdateProfileRequest] { request =>
        IO(println(s"Received UserSignUpRequest: $request")).unsafeRunSync() // Log incoming payload
        for {
          updatedProfile <-
            wandererProfileService.updateProfile(
              userId = userId,
              loginDetailsUpdate = request.loginDetails,
              addressUpdate = request.address,
              personalDetailsUpdate = request.personalDetails
            )
          response <-
            updatedProfile match {
              case Some(profile) => Ok(profile) // Return updated profile
              case None => NotFound("User not found")
            }
        } yield response
      }.handleErrorWith {
        case _: io.circe.Error => BadRequest("Invalid JSON payload")
      }
  }
}

object WandererProfileController {
  def apply[F[_] : Concurrent](
                                wandererProfileService: WandererProfileServiceAlgebra[F]
                              ): WandererProfileController[F] =
    new WandererProfileControllerImpl[F](wandererProfileService)
}
