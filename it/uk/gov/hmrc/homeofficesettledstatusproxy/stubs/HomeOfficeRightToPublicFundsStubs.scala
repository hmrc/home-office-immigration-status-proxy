package uk.gov.hmrc.homeofficesettledstatusproxy.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.homeofficesettledstatusproxy.support.WireMockSupport

trait HomeOfficeRightToPublicFundsStubs {
  me: WireMockSupport =>

  val validTokenForm = """grant_type=client_credentials&client_id=hmrc&client_secret=TBC"""

  val requestBodyWithRange: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "RJ301829A",
      |  "statusCheckRange": {
      |    "endDate": "2019-07-15",
      |    "startDate": "2019-04-15"
      |  }
      |}""".stripMargin

  val validRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "RJ301829A"
      |}""".stripMargin

  val invalidNinoRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "invailid"
      |}""".stripMargin

  val responseBodyWithStatus: String =
    """{
      |  "correlationId": "sjdfhks123",
      |  "result": {
      |    "dateOfBirth": "2001-01-31",
      |    "nationality": "IRL",
      |    "fullName": "Jane Doe",
      |    "statuses": [
      |      {
      |        "productType": "EUS",
      |        "immigrationStatus": "ILR",
      |        "noRecourseToPublicFunds": true,
      |        "statusEndDate": "2018-01-31",
      |        "statusStartDate": "2018-12-12"
      |      }
      |    ]
      |  }
      |}""".stripMargin

  def givenStatusCheckResultNoRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoStub(200, validRequestBody, responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoStub(200, requestBodyWithRange, responseBodyWithStatus)

  def givenEmptyStatusCheckResult(): StubMapping =
    givenStatusPublicFundsByNinoStub(200, validRequestBody, "")

  def givenStatusCheckErrorWhenMissingInputField(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(400, validRequestBody, errorResponseBody)
  }

  def givenStatusCheckErrorWhenInvalidJson(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(400, "[]", errorResponseBody)
  }

  def givenStatusCheckErrorWhenEmptyInput(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(400, "{}", errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(404, validRequestBody, errorResponseBody)
  }

  def givenStatusCheckErrorUndefined(status: Int): StubMapping = {

    assert(status >= 400 && status < 500)

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123"
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(status, validRequestBody, errorResponseBody)
  }

  def givenStatusCheckErrorWhenConflict(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_CONFLICT"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoStub(409, validRequestBody, errorResponseBody)

  }

  def givenStatusCheckErrorWhenDOBInvalid(): StubMapping = {

    val errorResponseBody: String =
      """{
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

    givenStatusPublicFundsByNinoStub(400, validRequestBody, errorResponseBody)

  }

  def givenOAuthTokenGranted(): StubMapping = {

    val oAuthTokenResponse: String =
      """{
        |   "access_token": "FOO0123456789",
        |   "refresh_token": "not-used",
        |   "token_type": "SomeTokenType"
        |}""".stripMargin

    givenStatusPublicFundsTokenStub(200, validTokenForm, oAuthTokenResponse)

  }

  def givenOAuthTokenDenied(): StubMapping =
    givenStatusPublicFundsTokenStub(401, validTokenForm, "")

  def givenStatusPublicFundsTokenStub(
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/token"))
        .withHeader("X-Correlation-Id", equalTo("sjdfhks123"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/x-www-form-urlencoded"))
        .withRequestBody(equalTo(requestBody))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

  def givenStatusPublicFundsByNinoStub(
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/nino"))
        .withHeader("X-Correlation-Id", equalTo("sjdfhks123"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withHeader(HeaderNames.AUTHORIZATION, containing("SomeTokenType FOO0123456789"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

}
