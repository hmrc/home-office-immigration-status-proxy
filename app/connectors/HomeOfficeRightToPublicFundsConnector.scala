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
import connectors.StatusCheckResponseHttpParser.*
import helpers.CorrelationIdHelper.*
import models.{OAuthToken, StatusCheckByMrzRequest, StatusCheckByNinoRequest, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import play.api.libs.json.Json
import play.api.libs.ws.{writeableOf_JsValue, writeableOf_urlEncodedForm}
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import wiring.AppConfig
import wiring.Constants.*

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject() (appConfig: AppConfig, http: HttpClientV2)(implicit
  ec: ExecutionContext
) {

  private def retrieveAuthToken(xCorrelationId: String, requestId: Option[RequestId]): Future[OAuthToken] = {
    val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders(
        HEADER_X_CORRELATION_ID -> xCorrelationId,
        "CorrelationId"         -> correlationId(requestId)
      )

    val form: Map[String, Seq[String]] = Map(
      "grant_type"    -> Seq("client_credentials"),
      "client_id"     -> Seq(appConfig.homeOfficeClientId),
      "client_secret" -> Seq(appConfig.homeOfficeClientSecret)
    )
    val url: URL =
      url"${appConfig.rightToPublicFundsBaseUrl}/v1/status/public-funds/token"
    http
      .post(url)(hc)
      .withBody(form)
      .withProxy
      .execute[OAuthToken]
  }

  def statusPublicFundsByNino(
    request: StatusCheckByNinoRequest,
    xCorrelationId: String,
    requestId: Option[RequestId]
  )(implicit hcc: HeaderCarrier): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] =
    retrieveAuthToken(xCorrelationId, hcc.requestId).flatMap { oAuthToken =>
      val url: URL =
        url"${appConfig.rightToPublicFundsBaseUrl}/v1/status/public-funds/nino"

      http
        .post(url)(
          getHeaderCarrier(xCorrelationId, requestId, oAuthToken)
        )
        .withBody(Json.toJson(request))
        .withProxy
        .execute[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]
    }

  def statusPublicFundsByMrz(
    request: StatusCheckByMrzRequest,
    xCorrelationId: String,
    requestId: Option[RequestId]
  )(implicit hcc: HeaderCarrier): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] =
    retrieveAuthToken(xCorrelationId, hcc.requestId).flatMap { oAuthToken =>
      val url: URL =
        url"${appConfig.rightToPublicFundsBaseUrl}/v1/status/public-funds/mrz"
      http
        .post(url)(
          getHeaderCarrier(xCorrelationId, requestId, oAuthToken)
        )
        .withBody(Json.toJson(request))
        .withProxy
        .execute[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]
    }

  private def getHeaderCarrier(xCorrelationId: String, requestId: Option[RequestId], token: OAuthToken): HeaderCarrier =
    HeaderCarrier().withExtraHeaders(
      HEADER_X_CORRELATION_ID   -> xCorrelationId,
      HeaderNames.authorisation -> s"${token.token_type} ${token.access_token}",
      "CorrelationId"           -> correlationId(requestId)
    )
}
