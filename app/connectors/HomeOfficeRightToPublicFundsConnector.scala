/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import com.google.inject.Singleton
import connectors.StatusCheckResponseHttpParser._
import models.{OAuthToken, StatusCheckByMrzRequest, StatusCheckByNinoRequest, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import wiring.AppConfig
import wiring.Constants._

import java.net.{URI, URL}
import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject() (appConfig: AppConfig, http: HttpClientV2) {

  def buildURL(path: String): URL = {
    val pathPrefix = if (appConfig.rightToPublicFundsBaseUrl.endsWith("/")) {
      appConfig.rightToPublicFundsBaseUrl
    } else {
      appConfig.rightToPublicFundsBaseUrl + "/"
    }
    val pathMiddle = if (appConfig.rightToPublicFundsPathPrefix.startsWith("/")) {
      appConfig.rightToPublicFundsPathPrefix.substring(1)
    } else {
      appConfig.rightToPublicFundsPathPrefix
    }
    val pathRest = if (path.startsWith("/")) {
      path
    } else {
      "/" + path
    }
    val uri = new URI(pathPrefix + pathMiddle + pathRest)
    uri.toURL
  }

  def token(xCorrelationId: String, requestId: Option[RequestId])(implicit ec: ExecutionContext): Future[OAuthToken] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders(
        HEADER_X_CORRELATION_ID -> xCorrelationId,
        "CorrelationId"         -> correlationId(requestId)
      )

    val url = buildURL("/status/public-funds/token")

    val form: Map[String, Seq[String]] = Map(
      "grant_type"    -> Seq("client_credentials"),
      "client_id"     -> Seq(appConfig.homeOfficeClientId),
      "client_secret" -> Seq(appConfig.homeOfficeClientSecret)
    )

    http.post(url).withBody(form).withProxy.execute[OAuthToken]
  }

  def statusPublicFundsByNino(
    request: StatusCheckByNinoRequest,
    xCorrelationId: String,
    requestId: Option[RequestId],
    token: OAuthToken
  )(implicit ec: ExecutionContext): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] = {

    implicit val hc: HeaderCarrier = getHeaderCarrier(xCorrelationId, requestId, token)

    val url = buildURL("/status/public-funds/nino")

    http
      .post(url)
      .withBody(Json.toJson(request))
      .withProxy
      .execute[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]
  }

  def statusPublicFundsByMrz(
    request: StatusCheckByMrzRequest,
    xCorrelationId: String,
    requestId: Option[RequestId],
    token: OAuthToken
  )(implicit ec: ExecutionContext): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] = {

    implicit val hc: HeaderCarrier = getHeaderCarrier(xCorrelationId, requestId, token)

    val url = buildURL("/status/public-funds/mrz")

    http
      .post(url)
      .withBody(Json.toJson(request))
      .withProxy
      .execute[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]
  }

  private def getHeaderCarrier(xCorrelationId: String, requestId: Option[RequestId], token: OAuthToken): HeaderCarrier =
    HeaderCarrier().withExtraHeaders(
      HEADER_X_CORRELATION_ID   -> xCorrelationId,
      HeaderNames.AUTHORIZATION -> s"${token.token_type} ${token.access_token}",
      "CorrelationId"           -> correlationId(requestId)
    )

  private[connectors] def generateNewUUID: String = randomUUID.toString

  private[connectors] def correlationId(requestId: Option[RequestId]): String = {
    val uuidLength = 24
    val CorrelationIdPattern =
      """.*([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}).*""".r
    requestId match {
      case Some(requestId) =>
        requestId.value match {
          case CorrelationIdPattern(prefix) => prefix + "-" + generateNewUUID.substring(uuidLength)
          case _                            => generateNewUUID
        }
      case _ => generateNewUUID
    }
  }
}
