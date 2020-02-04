package uk.gov.hmrc.homeofficesettledstatusproxy.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatusproxy.models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import uk.gov.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import uk.gov.hmrc.homeofficesettledstatusproxy.support.AppBaseISpec
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.ExecutionContext

class HomeOfficeRightToPublicFundsConnectorISpec
    extends AppBaseISpec with HomeOfficeRightToPublicFundsStubs {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val connector: HomeOfficeRightToPublicFundsConnector =
    app.injector.instanceOf[HomeOfficeRightToPublicFundsConnector]

  val dummyCorrelationId = "dummyCorrelationId"
  val dummyOAuthToken = OAuthToken("FOO0123456789", "not-used", "not-used", "SomeTokenType")

  val request = StatusCheckByNinoRequest("2001-01-31", "Jane", "Doe", Nino("RJ301829A"))

  "POST /v1/status/public-funds/token" should {
    "return valid oauth token" in {
      givenOAuthTokenGranted()
      val result: OAuthToken = await(connector.token())
      result.access_token shouldBe "FOO0123456789"
    }

    "raise exception if token denied" in {
      givenOAuthTokenDenied()
      an[Upstream4xxResponse] shouldBe thrownBy {
        await(connector.token())
      }
    }
  }

  "POST /v1/status/public-funds/nino" should {

    "return status when range provided" in {
      givenStatusCheckResultWithRangeExample()
      val request = StatusCheckByNinoRequest(
        "2001-01-31",
        "Jane",
        "Doe",
        Nino("RJ301829A"),
        Some(StatusCheckRange(Some("2019-07-15"), Some("2019-04-15"))))

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe defined
      result.error shouldBe None
    }

    "return status when no range provided" in {
      givenStatusCheckResultNoRangeExample()

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe defined
      result.error shouldBe None
    }

    "return check error when 400 response" in {
      givenStatusCheckErrorWhenMissingInputField()

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_REQUEST_INVALID"
    }

    "return check error when 404 response" in {
      givenStatusCheckErrorWhenStatusNotFound()

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_NOT_FOUND"
    }

    "return check error when 409 response" in {
      givenStatusCheckErrorWhenConflict()

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_CONFLICT"
    }

    "return check error when 422 response" in {
      givenStatusCheckErrorWhenDOBInvalid()

      val result: StatusCheckResponse =
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))

      result.result shouldBe None
      result.error shouldBe defined
      result.error.get.errCode.get shouldBe "ERR_VALIDATION"
    }

    "throw exception if other 4xx response" in {
      givenStatusPublicFundsByNinoStub(401, validRequestBody, "")

      an[Upstream4xxResponse] shouldBe thrownBy {
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))
      }
    }

    "throw exception if 5xx response" in {
      givenStatusPublicFundsByNinoStub(500, validRequestBody, "")

      an[Upstream5xxResponse] shouldBe thrownBy {
        await(connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken))
      }
    }
  }

}
