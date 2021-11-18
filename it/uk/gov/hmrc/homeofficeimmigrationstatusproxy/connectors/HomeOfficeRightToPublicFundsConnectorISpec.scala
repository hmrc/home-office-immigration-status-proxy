package connectors

import org.scalatest.concurrent.ScalaFutures
import connectors.ErrorCodes._
import uk.gov.hmrc.domain.Nino
import models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse, StatusCheckErrorResponseWithStatus}
import stubs.HomeOfficeRightToPublicFundsStubs
import support.AppBaseISpec
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import play.api.http.Status._

class HomeOfficeRightToPublicFundsConnectorISpec
    extends AppBaseISpec with HomeOfficeRightToPublicFundsStubs with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val connector: HomeOfficeRightToPublicFundsConnector =
    app.injector.instanceOf[HomeOfficeRightToPublicFundsConnector]

  val dummyCorrelationId = "sjdfhks123"
  val dummyOAuthToken: OAuthToken = OAuthToken("FOO0123456789", "SomeTokenType")

  val request: StatusCheckByNinoRequest = StatusCheckByNinoRequest("2001-01-31", "Jane", "Doe", Nino("RJ301829A"))

  "HomeOfficeRightToPublicFundsConnector.token" should {
    "return valid oauth token" in {
      givenOAuthTokenGranted()
      val result: OAuthToken = connector.token(dummyCorrelationId).futureValue
      result.access_token shouldBe "FOO0123456789"
    }

    "return valid oauth token without refresh token" in {
      givenOAuthTokenGrantedWithoutRefresh()
      val result: OAuthToken = connector.token(dummyCorrelationId).futureValue
      result.access_token shouldBe "FOO0123456789"
    }

    "raise exception if token denied" in {
      givenOAuthTokenDenied()
      val result = intercept[RuntimeException](connector.token(dummyCorrelationId).futureValue)
      result.getMessage should include("Upstream4xxResponse")
    }
  }

  "HomeOfficeRightToPublicFundsConnector.statusPublicFundsByNino" should {

    "return status when range provided" in {
      givenStatusCheckResultWithRangeExample()
      val request = StatusCheckByNinoRequest(
        "2001-01-31",
        "Jane",
        "Doe",
        Nino("RJ301829A"),
        Some(
          StatusCheckRange(
            Some(LocalDate.parse("2019-07-15")),
            Some(LocalDate.parse("2019-04-15")))))

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.right.get shouldBe responseBodyWithStatusObject
    }

    "return status when no range provided" in {
      givenStatusCheckResultNoRangeExample()

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.right.get shouldBe responseBodyWithStatusObject
    }

    "return check error when 400 response ERR_REQUEST_INVALID" in {
      givenStatusCheckErrorWhenMissingInputField()

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe BAD_REQUEST
      result.left.get.errorResponse.error.errCode shouldBe ERR_REQUEST_INVALID
    }

    "return check error when 404 response ERR_NOT_FOUND" in {
      givenStatusCheckErrorWhenStatusNotFound()

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe NOT_FOUND
      result.left.get.errorResponse.error.errCode shouldBe ERR_NOT_FOUND
    }

    "return check error when 409 response ERR_CONFLICT" in {
      givenStatusCheckErrorWhenConflict()

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe CONFLICT
      result.left.get.errorResponse.error.errCode shouldBe ERR_CONFLICT
    }

    "return check error when 400 response ERR_VALIDATION" in {
      givenStatusCheckErrorWhenDOBInvalid()

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe BAD_REQUEST
      result.left.get.errorResponse.error.errCode shouldBe ERR_VALIDATION
    }

    "return unknown error if other 4xx response" in {
      givenStatusPublicFundsByNinoStub(TOO_MANY_REQUESTS, validRequestBody, "")

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe TOO_MANY_REQUESTS
      result.left.get.errorResponse.error.errCode shouldBe ERR_UNKNOWN
    }

    "return unknown error if 5xx response" in {
      givenStatusPublicFundsByNinoStub(INTERNAL_SERVER_ERROR, validRequestBody, "")

      val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.left.get.statusCode shouldBe INTERNAL_SERVER_ERROR
      result.left.get.errorResponse.error.errCode shouldBe ERR_UNKNOWN
    }

  }

}
