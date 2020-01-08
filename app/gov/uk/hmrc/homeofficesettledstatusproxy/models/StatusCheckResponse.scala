package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class StatusCheckResponse(
  // Identifier associated with a checks,
  // if x-correlation-id is not provided in request headers, a new id generated using token service
  correlationId: String,
  // Represents an error occurred
  error: Option[StatusCheckError] = None,
  // Represents the result
  result: Option[StatusCheckResult] = None
)

object StatusCheckResponse {
  implicit val formats = Json.format[StatusCheckResponse]
}
