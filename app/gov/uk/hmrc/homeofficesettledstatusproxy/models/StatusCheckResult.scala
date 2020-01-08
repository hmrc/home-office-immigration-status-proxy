package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class StatusCheckResult(
  // Date of birth of person being checked in ISO 8601 format
  dateOfBirth: Option[String] = None,
  // Image of the person being checked
  facialImage: Option[String] = None,
  // Full name of person being checked
  fullName: Option[String] = None,
  // 'Right to public fund' statuses
  statuses: Option[List[ImmigrationStatus]] = None
)

object StatusCheckResult {
  implicit val formats = Json.format[StatusCheckResult]
}
