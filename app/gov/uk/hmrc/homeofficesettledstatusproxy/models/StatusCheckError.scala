package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class ValidationError(
  // Code of the validation failure
  code: String,
  // Name of the field
  name: String
)

object ValidationError {
  implicit val formats = Json.format[ValidationError]
}

case class StatusCheckError(
  // Top level error code
  errCode: Option[String] = None,
  // Field level validation errors
  fields: Option[List[ValidationError]] = None
)

object StatusCheckError {
  implicit val formats = Json.format[StatusCheckError]
}
