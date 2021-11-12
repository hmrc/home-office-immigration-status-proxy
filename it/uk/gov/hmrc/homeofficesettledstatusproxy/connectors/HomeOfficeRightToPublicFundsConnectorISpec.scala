package uk.gov.hmrc.homeofficesettledstatusproxy.connectors

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.homeofficesettledstatusproxy.connectors.ErrorCodes._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatusproxy.models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import uk.gov.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import uk.gov.hmrc.homeofficesettledstatusproxy.support.AppBaseISpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

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

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result should not be None
      result.error shouldBe None
    }

    "return status when no range provided" in {
      givenStatusCheckResultNoRangeExample()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result should not be None
      result.error shouldBe None
    }

    "return check error when 400 response ERR_REQUEST_INVALID" in {
      givenStatusCheckErrorWhenMissingInputField()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result shouldBe None
      result.error should not be None
      result.error.get.errCode shouldBe ERR_REQUEST_INVALID
    }

    "return check error when 404 response ERR_NOT_FOUND" in {
      givenStatusCheckErrorWhenStatusNotFound()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result shouldBe None
      result.error should not be None
      result.error.get.errCode shouldBe ERR_NOT_FOUND
    }

    "return check error when 409 response ERR_CONFLICT" in {
      givenStatusCheckErrorWhenConflict()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result shouldBe None
      result.error should not be None
      result.error.get.errCode shouldBe ERR_CONFLICT
    }

    "return check error when 400 response ERR_VALIDATION" in {
      givenStatusCheckErrorWhenDOBInvalid()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.result shouldBe None
      result.error should not be None
      result.error.get.errCode shouldBe ERR_VALIDATION
    }

    "throw exception if other 4xx response" in {
      givenStatusPublicFundsByNinoStub(429, validRequestBody, "")

      val result = intercept[RuntimeException] {
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue
      }
      result.getMessage should include("Upstream4xxResponse")
    }

    "throw exception if 5xx response" in {
      givenStatusPublicFundsByNinoStub(500, validRequestBody, "")

      val result = intercept[RuntimeException] {
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue
      }
      result.getMessage should include("Upstream5xxResponse")
    }

    "return empty response when status is not active" in {
      givenEmptyStatusCheckResult()

      val result: StatusCheckResponse =
        connector.statusPublicFundsByNino(request, dummyCorrelationId, dummyOAuthToken).futureValue

      result.correlationId shouldBe dummyCorrelationId
      result.result shouldBe None
      result.error shouldBe None
    }
  }

  "HomeOfficeRightToPublicFundsConnector.statusPublicFundsByNinoRaw" should {

    "return status when range provided" in {
      givenStatusCheckResultWithRangeExample()
      val request =
        StatusCheckByNinoRequest(
          "2001-01-31",
          "Jane",
          "Doe",
          Nino("RJ301829A"),
          Some(
            StatusCheckRange(
              Some(LocalDate.parse("2019-07-15")),
              Some(LocalDate.parse("2019-04-15")))))

      val response: HttpResponse =
        connector.statusPublicFundsByNinoRaw(
          Json.stringify(Json.toJson(request)),
          dummyCorrelationId,
          dummyOAuthToken).futureValue

      response.status shouldBe 200
      val result = Json.parse(response.body).as[StatusCheckResponse]
      result.result should not be None
      result.error shouldBe None
    }

    "return check error when 400 response ERR_REQUEST_INVALID" in {
      givenStatusCheckErrorWhenMissingInputField()

      val response: HttpResponse =
          connector.statusPublicFundsByNinoRaw(
            Json.stringify(Json.toJson(request)),
            dummyCorrelationId,
            dummyOAuthToken).futureValue

      response.status shouldBe 400
      val result = Json.parse(response.body).as[StatusCheckResponse]

      result.result shouldBe None
      result.error should not be None
      result.error.get.errCode shouldBe ERR_REQUEST_INVALID
    }

    "return check error when 404 response ERR_NOT_FOUND" in {
      givenStatusCheckErrorWhenStatusNotFound()

      val response: HttpResponse =
          connector.statusPublicFundsByNinoRaw(
            Json.stringify(Json.toJson(request)),
            dummyCorrelationId,
            dummyOAuthToken).futureValue

      response.status shouldBe 404
      response.body shouldBe """{
                               |  "correlationId": "sjdfhks123",
                               |  "error": {
                               |    "errCode": "ERR_NOT_FOUND"
                               |  }
                               |}""".stripMargin
    }

    "return check error when 409 response ERR_CONFLICT" in {
      givenStatusCheckErrorWhenConflict()

      val response: HttpResponse =
          connector.statusPublicFundsByNinoRaw(
            Json.stringify(Json.toJson(request)),
            dummyCorrelationId,
            dummyOAuthToken).futureValue

      response.status shouldBe 409
      response.body shouldBe """{
                               |  "correlationId": "sjdfhks123",
                               |  "error": {
                               |    "errCode": "ERR_CONFLICT"
                               |  }
                               |}""".stripMargin
    }

    "return check error when 400 response ERR_VALIDATION" in {
      givenStatusCheckErrorWhenDOBInvalid()

      val response: HttpResponse =
          connector.statusPublicFundsByNinoRaw(
            Json.stringify(Json.toJson(request)),
            dummyCorrelationId,
            dummyOAuthToken).futureValue

      response.status shouldBe 400
      response.body shouldBe """{
                               |  "correlationId": "sjdfhks123",
                               |  "error": {
                               |    "errCode": "ERR_VALIDATION",
                               |    "fields": [
                               |      {
                               |        "code": "ERR_INVALID_DOB",
                               |        "name": "dateOfBirth"
                               |      }
                               |    ]
                               |  }
                               |}""".stripMargin

    }

    "throw exception if other 4xx response" in {
      givenStatusPublicFundsByNinoStub(429, validRequestBody, "")

      val response: HttpResponse =
        connector.statusPublicFundsByNinoRaw(
          Json.stringify(Json.toJson(request)),
          dummyCorrelationId,
          dummyOAuthToken).futureValue

      response.status shouldBe 429
    }

    "throw exception if 5xx response" in {
      givenStatusPublicFundsByNinoStub(500, validRequestBody, "")

      val response: HttpResponse =
        connector.statusPublicFundsByNinoRaw(
          Json.stringify(Json.toJson(request)),
          dummyCorrelationId,
          dummyOAuthToken).futureValue

      response.status shouldBe 500
    }
  }

  val errorGenerator: HttpErrorFunctions = new HttpErrorFunctions {}

  "HomeOfficeRightToPublicFundsConnector.extractResponseBody" should {
    "return the json notFoundMessage if the prefix present" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.notFoundMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeRightToPublicFundsConnector
        .extractResponseBody(errorMessage, "Response body: '") shouldBe responseBody
    }

    "return the json badRequestMessage if the prefix present" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.badRequestMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeRightToPublicFundsConnector
        .extractResponseBody(errorMessage, "Response body '") shouldBe responseBody
    }

    "return the whole message if prefix missing" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.notFoundMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeRightToPublicFundsConnector
        .extractResponseBody(errorMessage, "::: '") shouldBe s"""{"error":{"errCode":"$errorMessage"}}"""
    }
  }

}
