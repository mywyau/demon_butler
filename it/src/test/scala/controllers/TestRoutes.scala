package controllers

import cats.effect.*
import cats.implicits.*
import cats.syntax.all.*
import controllers.business.{BusinessAddressController, BusinessContactDetailsController, BusinessSpecificationsController}
import controllers.business_listing.BusinessListingController
import controllers.desk_listing.DeskListingController
import controllers.office.{OfficeAddressController, OfficeContactDetailsController,OfficeSpecificationsController}
import controllers.office_listing.OfficeListingController
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import repositories.business.{BusinessAddressRepository, BusinessContactDetailsRepository, BusinessSpecificationsRepository}
import repositories.desk.DeskListingRepository
import repositories.office.{OfficeAddressRepository, OfficeContactDetailsRepository, OfficeSpecificationsRepository}
import services.business.address.BusinessAddressService
import services.business.business_listing.BusinessListingService
import services.business.contact_details.BusinessContactDetailsService
import services.business.specifications.BusinessSpecificationsService
import services.desk_listing.DeskListingService
import services.office.OfficeSpecificationsService
import services.office.address.OfficeAddressService
import services.office.contact_details.OfficeContactDetailsService
import services.office.office_listing.OfficeListingService

import java.time.LocalDateTime

object TestRoutes {

  implicit val testLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def businessContactDetailsRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val businessContactDetailsRepository = BusinessContactDetailsRepository(transactor)

    val businessContactDetailsService = BusinessContactDetailsService(businessContactDetailsRepository)
    val businessContactDetailsController = BusinessContactDetailsController(businessContactDetailsService)

    businessContactDetailsController.routes
  }


  def businessAddressRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val businessAddressRepository = BusinessAddressRepository(transactor)

    val businessAddressService = BusinessAddressService(businessAddressRepository)
    val businessAddressController = BusinessAddressController(businessAddressService)

    businessAddressController.routes
  }

  def businessSpecificationsRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val businessSpecificationsRepository = BusinessSpecificationsRepository(transactor)

    val businessSpecificationsService = BusinessSpecificationsService(businessSpecificationsRepository)
    val businessSpecificationsController = BusinessSpecificationsController(businessSpecificationsService)

    businessSpecificationsController.routes
  }

  def businessListingRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val businessAddressRepository = BusinessAddressRepository(transactor)
    val businessContactDetailsRepository = BusinessContactDetailsRepository(transactor)
    val businessSpecificationsRepository = BusinessSpecificationsRepository(transactor)

    val businessListingService = BusinessListingService(businessAddressRepository, businessContactDetailsRepository, businessSpecificationsRepository)
    val businessListingController = BusinessListingController(businessListingService)

    businessListingController.routes
  }

  def deskListingRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val deskListingRepository = DeskListingRepository(transactor)
    val deskListingService = DeskListingService(deskListingRepository)
    val deskListingController = DeskListingController(deskListingService)

    deskListingController.routes
  }

  def officeAddressRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val officeAddressRepository = OfficeAddressRepository(transactor)

    val officeAddressService = OfficeAddressService(officeAddressRepository)
    val officeAddressController = OfficeAddressController(officeAddressService)

    officeAddressController.routes
  }

  def officeContactDetailsRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val officeContactDetailsRepository = OfficeContactDetailsRepository(transactor)

    val officeContactDetailsService = OfficeContactDetailsService(officeContactDetailsRepository)
    val officeContactDetailsController = OfficeContactDetailsController(officeContactDetailsService)

    officeContactDetailsController.routes
  }


  def officeSpecificationsRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val officeSpecificationsRepository = OfficeSpecificationsRepository(transactor)

    val officeSpecificationsService = OfficeSpecificationsService(officeSpecificationsRepository)
    val officeSpecificationsController = OfficeSpecificationsController(officeSpecificationsService)

    officeSpecificationsController.routes
  }

  def officeListingRoutes(transactor: Transactor[IO]): HttpRoutes[IO] = {

    val officeAddressRepository = OfficeAddressRepository(transactor)
    val officeContactDetailsRepository = OfficeContactDetailsRepository(transactor)
    val officeSpecsRepository = OfficeSpecificationsRepository(transactor)

    val officeListingService = OfficeListingService(officeAddressRepository, officeContactDetailsRepository, officeSpecsRepository)
    val officeListingController = OfficeListingController(officeListingService)

    officeListingController.routes
  }

  def createTestRouter(transactor: Transactor[IO]): HttpRoutes[IO] = {

    Router(
      "/pistachio" -> (
        businessAddressRoutes(transactor) <+>
          businessContactDetailsRoutes(transactor) <+>
          businessSpecificationsRoutes(transactor) <+>
          businessListingRoutes(transactor) <+>
          officeAddressRoutes(transactor) <+>
          officeContactDetailsRoutes(transactor) <+>
          officeSpecificationsRoutes(transactor) <+>
          officeListingRoutes(transactor) <+>
          deskListingRoutes(transactor)
        )
    )
  }
}
