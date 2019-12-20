package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.Json

case class HomeOfficeSettledStatusProxyModel(
  parameter1: String,
  parameter2: Option[String],
  telephoneNumber: Option[String],
  emailAddress: Option[String])

object HomeOfficeSettledStatusProxyModel {
  implicit val modelFormat = Json.format[HomeOfficeSettledStatusProxyModel]
}
