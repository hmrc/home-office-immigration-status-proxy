package gov.uk.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.{Format, Json}

case class OAuthToken(
  access_token: String,
  refresh_token: String,
  id_token: String,
  token_type: String)

object OAuthToken {
  implicit val formats: Format[OAuthToken] = Json.format[OAuthToken]
}
