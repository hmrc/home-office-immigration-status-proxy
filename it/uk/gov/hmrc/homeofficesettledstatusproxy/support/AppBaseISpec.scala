package uk.gov.hmrc.homeofficesettledstatusproxy.support

import org.scalatestplus.play.OneAppPerSuite
import play.api.Application

abstract class AppBaseISpec extends BaseISpec with OneAppPerSuite with TestApplication {

  override implicit lazy val app: Application = appBuilder.build()

}
