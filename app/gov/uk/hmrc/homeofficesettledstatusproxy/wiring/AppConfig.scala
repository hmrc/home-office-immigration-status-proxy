package gov.uk.hmrc.homeofficesettledstatusproxy.wiring

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {}

class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {}
