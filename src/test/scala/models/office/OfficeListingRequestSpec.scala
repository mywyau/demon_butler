package models.office

import cats.effect.IO
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.EncoderOps
import models.office.address_details.requests.OfficeAddressRequest
import models.office.adts.*
import models.office.contact_details.OfficeContactDetails
import models.office.office_listing.requests.OfficeListingRequest
import models.office.specifications.{OfficeAvailability, OfficeSpecifications}
import weaver.SimpleIOSuite

import java.time.{LocalDateTime, LocalTime}

object OfficeListingRequestSpec extends SimpleIOSuite {

  val testOfficeSpecs: OfficeSpecifications =
    OfficeSpecifications(
      id = Some(1),
      businessId = "business_id_1",
      officeId = "office_id_1",
      officeName = "Modern Workspace",
      description = "A vibrant office space in the heart of the city, ideal for teams or individuals.",
      officeType = OpenPlanOffice,
      numberOfFloors = 3,
      totalDesks = 3,
      capacity = 50,
      amenities = List("Wi-Fi", "Coffee Machine", "Projector", "Whiteboard", "Parking"),
      availability =
        OfficeAvailability(
          days = List("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
          startTime = LocalTime.of(10, 0, 0),
          endTime = LocalTime.of(10, 30, 0)
        ),
      rules = Some("No smoking. Maintain cleanliness."),
      createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
      updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    )

  val testOfficeAddressRequest =
    OfficeAddressRequest(
      businessId = "business_id_1",
      officeId = "office_id_1",
      buildingName = Some("build_123"),
      floorNumber = Some("floor 1"),
      street = Some("123 Main Street"),
      city = Some("New York"),
      country = Some("USA"),
      county = Some("New York County"),
      postcode = Some("10001"),
      latitude = Some(100.1),
      longitude = Some(-100.1)
    )

  val testOfficeContactDetails =
    OfficeContactDetails(
      id = Some(1),
      businessId = "business_id_1",
      officeId = "office_id_1",
      primaryContactFirstName = "Michael",
      primaryContactLastName = "Yau",
      contactEmail = "mike@gmail.com",
      contactNumber = "07402205071",
      createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
      updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    )

  val officeListingRequest =
    OfficeListingRequest(
      officeId = "office_id_1",
      addressDetails = testOfficeAddressRequest,
      officeSpecs = testOfficeSpecs,
      contactDetails = testOfficeContactDetails,
      createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
      updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    )

  test("OfficeListingRequest model encodes correctly to JSON") {

    val jsonResult = officeListingRequest.asJson

    val expectedJson =
      """
        |{
        |  "officeId": "office_id_1",
        |  "officeSpecs": {
        |    "id": 1,
        |    "businessId": "business_id_1",
        |    "officeId": "office_id_1",
        |    "officeName": "Modern Workspace",
        |    "description": "A vibrant office space in the heart of the city, ideal for teams or individuals.",
        |    "officeType": "OpenPlanOffice",
        |    "numberOfFloors": 3,
        |    "totalDesks": 3,
        |    "capacity": 50,
        |    "availability": {
        |      "days": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"],
        |      "startTime": "10:00:00",
        |      "endTime": "10:30:00"
        |    },
        |    "amenities": ["Wi-Fi", "Coffee Machine", "Projector", "Whiteboard", "Parking"],
        |    "rules": "No smoking. Maintain cleanliness.",
        |    "createdAt": "2025-01-01T00:00:00",
        |    "updatedAt": "2025-01-01T00:00:00"
        |  },
        |  "addressDetails": {
        |    "businessId": "business_id_1",
        |    "officeId": "office_id_1",
        |    "buildingName": "build_123",
        |    "floorNumber": "floor 1",
        |    "street": "123 Main Street",
        |    "city": "New York",
        |    "country": "USA",
        |    "county": "New York County",
        |    "postcode": "10001",
        |    "latitude": 100.1,
        |    "longitude": -100.1
        |  },
        |  "contactDetails": {
        |    "id": 1,
        |    "businessId": "business_id_1",
        |    "officeId": "office_id_1",
        |    "primaryContactFirstName": "Michael",
        |    "primaryContactLastName": "Yau",
        |    "contactEmail": "mike@gmail.com",
        |    "contactNumber": "07402205071",
        |    "createdAt": "2025-01-01T00:00:00",
        |    "updatedAt": "2025-01-01T00:00:00"
        |  },
        |  "createdAt": "2025-01-01T00:00:00",
        |  "updatedAt": "2025-01-01T00:00:00"
        |}
        |""".stripMargin

    val expectedResult: Json = parse(expectedJson).getOrElse(Json.Null)

    for {
      _ <- IO("")
//      _ <- IO(println(jsonResult))
    } yield {
      expect(jsonResult == expectedResult)
    }
  }

}

