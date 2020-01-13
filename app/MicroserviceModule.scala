import com.google.inject.AbstractModule
import gov.uk.hmrc.homeofficesettledstatusproxy.wiring.ProxyHttpClient
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http._

class MicroserviceModule(val environment: Environment, val configuration: Configuration)
    extends AbstractModule {

  def configure(): Unit = {
    val appName = "home-office-settled-status-proxy"
    Logger(getClass).info(s"Starting microservice : $appName : in mode : ${environment.mode}")

    bind(classOf[HttpGet]).to(classOf[ProxyHttpClient])
    bind(classOf[HttpPost]).to(classOf[ProxyHttpClient])
  }
}
