/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import wiring.{AppConfig, ProxyHttpClient}
import uk.gov.hmrc.http._
import scala.concurrent.{ExecutionContext, Future}
import connectors.StatusCheckResponseHttpParser._

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject()(
  appConfig: AppConfig,
  http: ProxyHttpClient,
  metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  def token(correlationId: String)(implicit ec: ExecutionContext): Future[OAuthToken] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders(HEADER_X_CORRELATION_ID -> correlationId)

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/token").toString

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
    correlationId: String,
    token: OAuthToken)(implicit ec: ExecutionContext)
    : Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders(
        HEADER_X_CORRELATION_ID   -> correlationId,
        HeaderNames.AUTHORIZATION -> s"${token.token_type} ${token.access_token}")

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/nino").toString

    val headers = Seq()

    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Nino") {
      http
        .POST[
          StatusCheckByNinoRequest,
          Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]](url, request, headers)
    }
  }

}
