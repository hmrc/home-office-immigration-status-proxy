package gov.uk.hmrc.homeofficesettledstatusproxy.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.google.inject.Singleton
import com.kenshoo.play.metrics.Metrics
import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckResponse}
import gov.uk.hmrc.homeofficesettledstatusproxy.wiring.AppConfig
import javax.inject.Inject
import play.api.libs.json.Json
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpPost, NotFoundException, Upstream4xxResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject()(
  appConfig: AppConfig,
  http: HttpPost,
  metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def statusPublicFundsByNino(request: StatusCheckByNinoRequest, correlationId: String)(
    implicit c: HeaderCarrier,
    ec: ExecutionContext): Future[StatusCheckResponse] = {
    val url = new URL(
      appConfig.rightToPublicFundsBaseUrl,
      appConfig.rightToPublicFundsPathPrefix + "/status/public-funds/nino").toString
    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Nino") {
      http
        .POST[StatusCheckByNinoRequest, StatusCheckResponse](url, request)
        .recover {
          case e: BadRequestException =>
            Json.parse(extractResponseBody(e.message, "Response body '")).as[StatusCheckResponse]
          case e: NotFoundException =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
          case e: Upstream4xxResponse if e.upstreamResponseCode == 422 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
        }
    }
  }

  def extractResponseBody(message: String, prefix: String): String = {
    val pos = message.indexOf(prefix)
    val body =
      if (pos >= 0) message.substring(pos + prefix.length, message.length - 1)
      else s"""{"error":{"errCode":"$message"}}"""
    body
  }

}
