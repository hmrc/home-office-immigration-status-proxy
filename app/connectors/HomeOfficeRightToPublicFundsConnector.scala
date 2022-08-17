/*
 * Copyright 2022 HM Revenue & Customs
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

import java.net.URL
import com.codahale.metrics.MetricRegistry
import com.google.inject.Singleton
import com.kenshoo.play.metrics.Metrics

import javax.inject.Inject
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import models.{OAuthToken, StatusCheckByMrzRequest, StatusCheckByNinoRequest, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import wiring.{AppConfig, ProxyHttpClient}
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}
import connectors.StatusCheckResponseHttpParser._
import HttpReads.Implicits._
import wiring.Constants._

import java.util.UUID.randomUUID

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject() (appConfig: AppConfig, http: ProxyHttpClient, metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def token(xCorrelationId: String, requestId: Option[RequestId])(implicit ec: ExecutionContext): Future[OAuthToken] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders(
        HEADER_X_CORRELATION_ID -> xCorrelationId,
        "CorrelationId"         -> correlationId(requestId)
      )

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/token"
    ).toString

    val form: Map[String, Seq[String]] = Map(
      "grant_type"    -> Seq("client_credentials"),
      "client_id"     -> Seq(appConfig.homeOfficeClientId),
      "client_secret" -> Seq(appConfig.homeOfficeClientSecret)
    )
    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-Token") {
      http.POSTForm[OAuthToken](url, form)
    }
  }

  def statusPublicFundsByNino(
    request: StatusCheckByNinoRequest,
    xCorrelationId: String,
    requestId: Option[RequestId],
    token: OAuthToken
  )(implicit ec: ExecutionContext): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] = {

    implicit val hc: HeaderCarrier = getHeaderCarrier(xCorrelationId, requestId, token)

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/nino"
    ).toString

    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Nino") {
      http
        .POST[StatusCheckByNinoRequest, Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]](url, request)
    }
  }

  def statusPublicFundsByMrz(
    request: StatusCheckByMrzRequest,
    xCorrelationId: String,
    requestId: Option[RequestId],
    token: OAuthToken
  )(implicit ec: ExecutionContext): Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] = {

    implicit val hc: HeaderCarrier = getHeaderCarrier(xCorrelationId, requestId, token)

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/mrz"
    ).toString

    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Mrz") {
      http
        .POST[StatusCheckByMrzRequest, Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]](url, request)
    }
  }

  private def getHeaderCarrier(xCorrelationId: String, requestId: Option[RequestId], token: OAuthToken) =
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
