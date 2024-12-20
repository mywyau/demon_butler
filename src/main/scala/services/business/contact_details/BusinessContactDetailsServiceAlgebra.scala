package services.business.contact_details

import models.business.contact_details.BusinessContactDetails
import models.business.contact_details.errors.BusinessContactDetailsErrors

trait BusinessContactDetailsServiceAlgebra[F[_]] {

  def getContactDetailsByBusinessId(businessId: String): F[Either[BusinessContactDetailsErrors, BusinessContactDetails]]

  def createBusinessContactDetails(businessContactDetails: BusinessContactDetails): F[cats.data.ValidatedNel[BusinessContactDetailsErrors, Int]]
}