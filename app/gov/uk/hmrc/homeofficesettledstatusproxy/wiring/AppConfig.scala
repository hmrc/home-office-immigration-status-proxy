package gov.uk.hmrc.homeofficesettledstatusproxy.wiring

import java.net.URL

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {

  val rightToPublicFundsBaseUrl: URL
}

class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {

  val rightToPublicFundsBaseUrl: URL =
    new URL(config.baseUrl("home-office-right-to-public-funds"))
}
