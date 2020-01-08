package gov.uk.hmrc.homeofficesettledstatusproxy.connectors

import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import gov.uk.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import gov.uk.hmrc.homeofficesettledstatusproxy.support.{BaseISpec, TestApplication}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class HomeOfficeRightToPublicFundsConnectorISpec
    extends BaseISpec with TestApplication with HomeOfficeRightToPublicFundsStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val controller: HomeOfficeRightToPublicFundsConnector =
    app.injector.instanceOf[HomeOfficeRightToPublicFundsConnector]

  "GET /status/public-funds/nino " should {
    "return status when range provided" in {
      givenStatusCheckResultWithRangeExample()
      val request = StatusCheckByNinoRequest(
        "2001-01-01",
        "Jane",
        "Doe",
        "sc087676868",
        Some(StatusCheckRange(Some("2019-07-15"), Some("2019-04-15"))))
      val result: StatusCheckResponse = await(controller.statusPublicFundsByNino(request))
      result.result shouldBe defined
      result.error shouldBe None
    }

    "return status when no range provided" in {
      givenStatusCheckResultNoRangeExample()
      val request = StatusCheckByNinoRequest("2001-01-01", "Jane", "Doe", "sc087676868")
      val result: StatusCheckResponse = await(controller.statusPublicFundsByNino(request))
      result.result shouldBe defined
      result.error shouldBe None
    }

    "return check error" in {
      givenStatusCheckErrorExample()
      val request = StatusCheckByNinoRequest("2001-01-01", "Jane", "Doe", "sc087676868")
      val result: StatusCheckResponse = await(controller.statusPublicFundsByNino(request))
      result.result shouldBe None
      result.error shouldBe defined
    }
  }

}
