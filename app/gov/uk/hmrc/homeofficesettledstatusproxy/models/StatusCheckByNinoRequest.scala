package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class StatusCheckByNinoRequest(
  // Date of birth of the person being checked in ISO 8601 format (can contain wildcards for day or month)
  dateOfBirth: String,
  // Family name required for search
  familyName: String,
  // Given name required for search
  givenName: String,
  // National insurance number
  nino: String,
  statusCheckRange: Option[StatusCheckRange] = None
)

object StatusCheckByNinoRequest {
  implicit val formats = Json.format[StatusCheckByNinoRequest]
}
