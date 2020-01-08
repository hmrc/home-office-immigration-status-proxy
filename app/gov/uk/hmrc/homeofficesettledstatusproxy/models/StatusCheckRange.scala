package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class StatusCheckRange(
  // Requested status end date in ISO 8601 format
  endDate: Option[String] = None,
  // Requested status start date in ISO 8601 format
  startDate: Option[String] = None
)

object StatusCheckRange {
  implicit val formats = Json.format[StatusCheckRange]
}
