package services.wanderer_address

import cats.effect.IO
import models.users.*
import models.users.wanderer_address.errors.AddressNotFound
import models.users.wanderer_address.service.WandererAddress
import repositories.users.WandererAddressRepositoryAlgebra
import weaver.SimpleIOSuite

import java.time.LocalDateTime

object WandererAddressServiceSpec extends SimpleIOSuite {

  def testAddress(id: Option[Int], user_id: String): WandererAddress =
    WandererAddress(
      id = Some(1),
      user_id = user_id,
      street = "fake street 1",
      city = "fake city 1",
      country = "UK",
      county = Some("County 1"),
      postcode = "CF3 3NJ",
      created_at = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
      updated_at = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    )

  class MockWandererAddressRepository(
                                       existingWandererAddress: Map[String, WandererAddress] = Map.empty
                                     ) extends WandererAddressRepositoryAlgebra[IO] {

    def showAllUsers: IO[Map[String, WandererAddress]] = IO.pure(existingWandererAddress)

    override def createUserAddress(user: WandererAddress): IO[Int] = IO.pure(1) // Assume user creation always succeeds

    override def findByUserId(user_id: String): IO[Option[WandererAddress]] = IO.pure(existingWandererAddress.get(user_id))
  }


  test(".getAddressDetailsByUserId() - when there is an existing user address details given a user_id should return the correct address details - Right(address)") {

    val existingAddressForUser = testAddress(Some(1), "user_id_1")

    val mockWandererAddressRepository = new MockWandererAddressRepository(Map("user_id_1" -> existingAddressForUser))
    val service = new WandererAddressServiceImpl[IO](mockWandererAddressRepository)

    for {
      result <- service.getAddressDetailsByUserId("user_id_1")
    } yield {
      expect(result == Right(existingAddressForUser))
    }
  }

  test(".getAddressDetailsByUserId() - when there are no existing user address details given a user_id should return Left(AddressNotFound)") {

    val existingAddressForUser = testAddress(Some(1), "user_id_1")

    val mockWandererAddressRepository = new MockWandererAddressRepository(Map())
    val service = new WandererAddressServiceImpl[IO](mockWandererAddressRepository)

    for {
      result <- service.getAddressDetailsByUserId("user_id_1")
    } yield {
      expect(result == Left(AddressNotFound))
    }
  }

  test(".created() - when given a WandererAddress successfully create the address") {

    val sampleAddress = testAddress(Some(1), "user_id_1")

    val mockWandererAddressRepository = new MockWandererAddressRepository(Map())
    val service = WandererAddressService(mockWandererAddressRepository)

    for {
      result <- service.createAddress(sampleAddress)
    } yield {
      expect(result == 1)
    }
  }
}
