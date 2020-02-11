/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatusproxy.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.google.inject.Singleton
import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.homeofficesettledstatusproxy.connectors.HomeOfficeRightToPublicFundsConnector.extractResponseBody
import uk.gov.hmrc.homeofficesettledstatusproxy.models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckResponse}
import uk.gov.hmrc.homeofficesettledstatusproxy.wiring.AppConfig
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject()(
  appConfig: AppConfig,
  http: HttpPost,
  metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def token()(implicit ec: ExecutionContext): Future[OAuthToken] = {

    implicit val hc: HeaderCarrier = HeaderCarrier()

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
    token: OAuthToken)(implicit ec: ExecutionContext): Future[StatusCheckResponse] = {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/nino").toString
    val headers = Seq(HeaderNames.AUTHORIZATION -> s"${token.token_type} ${token.access_token}")

    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Nino") {
      http
        .POST[StatusCheckByNinoRequest, StatusCheckResponse](url, request, headers)
        .recover {
          case e: BadRequestException =>
            Json.parse(extractResponseBody(e.message, "Response body '")).as[StatusCheckResponse]
          case e: NotFoundException =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
          case e: Upstream4xxResponse if e.upstreamResponseCode == 409 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
        }
    }
  }

}

object HomeOfficeRightToPublicFundsConnector {

  def extractResponseBody(message: String, prefix: String): String = {
    val pos = message.indexOf(prefix)
    val body =
      if (pos >= 0) message.substring(pos + prefix.length, message.length - 1)
      else s"""{"error":{"errCode":"$message"}}"""
    body
  }

}
