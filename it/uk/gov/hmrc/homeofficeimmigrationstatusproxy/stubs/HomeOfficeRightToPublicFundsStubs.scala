package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.mvc.Http.HeaderNames
import support.WireMockSupport
import models.{StatusCheckResponse, StatusCheckResult, ImmigrationStatus}
import java.time.LocalDate

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

  val mrzRequestBodyWithRange: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "documentNumber": "1234567890",
      |  "documentType": "PASSPORT",
      |  "nationality": "USA",
      |  "statusCheckRange": {
      |    "endDate": "2019-07-15",
      |    "startDate": "2019-04-15"
      |  }
      |}""".stripMargin


  val validNinoRequestBody: String =
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

  val validMrzRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "documentNumber": "1234567890",
      |  "documentType": "PASSPORT",
      |  "nationality": "USA"
      |}""".stripMargin

  val invalidMrzRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "documentNumber": "Jane",
      |  "documentType": "Doe"
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
  val responseBodyWithStatusObject = StatusCheckResponse(
        correlationId = "sjdfhks123",
       result = StatusCheckResult(
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        fullName = "Jane Doe",
        statuses = List(
            ImmigrationStatus(
              productType = "EUS",
              immigrationStatus = "ILR",
              noRecourseToPublicFunds = true,
              statusEndDate = Some(LocalDate.parse("2018-01-31")),
              statusStartDate = LocalDate.parse("2018-12-12")
            )
          )
        )
       )

  def getValidRequest(requestType: RequestType) = requestType match {
    case RequestType.Nino => validNinoRequestBody
    case RequestType.Mrz => validMrzRequestBody
  }

  def getRequestWithRange(requestType: RequestType) = requestType match {
    case RequestType.Nino => requestBodyWithRange
    case RequestType.Mrz => mrzRequestBodyWithRange
  }

  def givenStatusCheckResultNoRangeExample(requestType: RequestType): StubMapping =
    givenSearchStub(requestType, 200, getValidRequest(requestType), responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(requestType: RequestType): StubMapping =
    givenSearchStub(requestType, 200, getRequestWithRange(requestType), responseBodyWithStatus)

  def givenStatusCheckErrorWhenMissingInputField(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, 400, getValidRequest(requestType), errorResponseBody)
  }

  def givenStatusCheckErrorWhenInvalidJson(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, 400, "[]", errorResponseBody)
  }

  def givenStatusCheckErrorWhenEmptyInput(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, 400, "{}", errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, 404, getValidRequest(requestType), errorResponseBody)
  }

  def givenStatusCheckErrorUndefined(status: Int, requestType: RequestType): StubMapping = {

    assert(status >= 400 && status < 500)

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123"
        |}""".stripMargin

    givenSearchStub(requestType, status, getValidRequest(requestType), errorResponseBody)
  }

  def givenStatusCheckErrorWhenConflict(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_CONFLICT"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, 409, getValidRequest(requestType), errorResponseBody)

  }

  def givenStatusCheckErrorWhenDOBInvalid(requestType: RequestType): StubMapping = {

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

    givenSearchStub(requestType, 400, getValidRequest(requestType), errorResponseBody)

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

  def givenOAuthTokenGrantedWithoutRefresh(): StubMapping = {

    val oAuthTokenResponse: String =
      """{
        |   "access_token": "FOO0123456789",
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

  sealed trait RequestType
  object RequestType {
    case object Mrz extends RequestType
    case object Nino extends RequestType
  }

  def givenSearchStub(
    requestType: RequestType,
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    requestType match {
      case RequestType.Mrz => givenStatusPublicFundsByMrzStub(httpResponseCode, requestBody, responseBody)
      case RequestType.Nino => givenStatusPublicFundsByNinoStub(httpResponseCode, requestBody, responseBody)
    }


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
            .withHeader("X-Correlation-Id", "sjdfhks123")
            .withBody(responseBody)
        ))

  def givenStatusPublicFundsByMrzStub(
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/mrz"))
        .withHeader("X-Correlation-Id", equalTo("sjdfhks123"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withHeader(HeaderNames.AUTHORIZATION, containing("SomeTokenType FOO0123456789"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Correlation-Id", "sjdfhks123")
            .withBody(responseBody)
        ))


}
