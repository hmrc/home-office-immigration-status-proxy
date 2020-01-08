package gov.uk.hmrc.homeofficesettledstatusproxy.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.google.inject.Singleton
import com.kenshoo.play.metrics.Metrics
import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckResponse}
import gov.uk.hmrc.homeofficesettledstatusproxy.wiring.AppConfig
import javax.inject.Inject
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeRightToPublicFundsConnector @Inject()(appConfig: AppConfig, http: HttpPost, metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def statusPublicFundsByNino(
    request: StatusCheckByNinoRequest)(implicit c: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponse] =
    monitor(s"ConsumedAPI-Home-Office-Right-To-Public-Funds-Status-By-Nino") {
      http
        .POST[StatusCheckByNinoRequest, StatusCheckResponse](
          new URL(appConfig.rightToPublicFundsBaseUrl, "/status/public-funds/nino").toString,
          request)
    }

}
