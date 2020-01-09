package gov.uk.hmrc.homeofficesettledstatusproxy.connectors

import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import gov.uk.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import gov.uk.hmrc.homeofficesettledstatusproxy.support.AppBaseISpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.ExecutionContext

class HomeOfficeRightToPublicFundsConnectorISpec
    extends AppBaseISpec with HomeOfficeRightToPublicFundsStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val controller: HomeOfficeRightToPublicFundsConnector =
    app.injector.instanceOf[HomeOfficeRightToPublicFundsConnector]

  val dummyCorrelationId = "dummyCorrelationId"

  val request = StatusCheckByNinoRequest("2001-01-31", "Jane", "Doe", Nino("RJ301829A"))

  "GET /status/public-funds/nino " should {

    "return status when range provided" in {
      givenStatusCheckResultWithRangeExample()
      val request = StatusCheckByNinoRequest(
        "2001-01-31",
        "Jane",
        "Doe",
        Nino("RJ301829A"),
        Some(StatusCheckRange(Some("2019-07-15"), Some("2019-04-15"))))

      val result: StatusCheckResponse =
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))

      result.result shouldBe defined
      result.error shouldBe None
    }

    "return status when no range provided" in {
      givenStatusCheckResultNoRangeExample()

      val result: StatusCheckResponse =
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))

      result.result shouldBe defined
      result.error shouldBe None
    }

    "return check error when 400 response" in {
      givenStatusCheckErrorWhenMissingInputField()

      val result: StatusCheckResponse =
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_REQUEST_INVALID"
    }

    "return check error when 404 response" in {
      givenStatusCheckErrorWhenStatusNotFound()

      val result: StatusCheckResponse =
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_NOT_FOUND"
    }

    "return check error when 422 response" in {
      givenStatusCheckErrorWhenDOBInvalid()

      val result: StatusCheckResponse =
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_VALIDATION"
    }

    "throw exception if other 4xx response" in {
      givenStatusPublicFundsByNinoResponds(401, validRequestBody, "")

      an[Upstream4xxResponse] shouldBe thrownBy {
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))
      }
    }

    "throw exception if 5xx response" in {
      givenStatusPublicFundsByNinoResponds(500, validRequestBody, "")

      an[Upstream5xxResponse] shouldBe thrownBy {
        await(controller.statusPublicFundsByNino(request, dummyCorrelationId))
      }
    }
  }

}
