package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class ImmigrationStatus(
  // Underlying Immigration Status
  immigrationStatus: Option[String] = None,
  // 'Right to public funds status
  rightToPublicFunds: Option[Boolean] = None,
  // Expiry date of the 'right to public fund' Status in ISO 8601 format
  statusEndDate: Option[String] = None,
  // Start date of the 'right to public fund' Status in ISO 8601 format
  statusStartDate: Option[String] = None
)

object ImmigrationStatus {
  implicit val formats = Json.format[ImmigrationStatus]
}
