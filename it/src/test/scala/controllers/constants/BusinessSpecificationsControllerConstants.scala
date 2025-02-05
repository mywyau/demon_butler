package controllers.constants

import cats.effect.*
import models.business.address.BusinessAddress
import models.business.adts.*
import models.business.specifications.requests.CreateBusinessSpecificationsRequest
import models.business.specifications.{BusinessAvailability, BusinessSpecifications}

import java.time.LocalDateTime

object BusinessSpecificationsControllerConstants {

  val testBusinessSpecs: BusinessSpecifications =
    BusinessSpecifications(
      id = Some(1),
      userId = "user_id_1",
      businessId = "business_id_1",
      businessName = "Example Business Name",
      description = "some description",
      createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
      updatedAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    )


  def testCreateBusinessSpecificationsRequest(
                                               userId: String,
                                               businessId: String
                                             ): CreateBusinessSpecificationsRequest =
    CreateBusinessSpecificationsRequest(
      userId = userId,
      businessId = businessId,
      businessName = "Example Business Name",
      description = "some description"
    )
}
