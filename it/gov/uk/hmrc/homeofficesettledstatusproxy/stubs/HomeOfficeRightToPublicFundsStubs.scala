package gov.uk.hmrc.homeofficesettledstatusproxy.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import gov.uk.hmrc.homeofficesettledstatusproxy.support.WireMockSupport

trait HomeOfficeRightToPublicFundsStubs {
  me: WireMockSupport =>

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
      |    "facialImage": "string",
      |    "fullName": "Jane Doe",
      |    "statuses": [
      |      {
      |        "immigrationStatus": "ILR",
      |        "rightToPublicFunds": true,
      |        "statusEndDate": "2018-01-31",
      |        "statusStartDate": "2018-12-12"
      |      }
      |    ]
      |  }
      |}""".stripMargin

  def givenStatusCheckResultNoRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoResponds(200, validRequestBody, responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(): StubMapping =
    givenStatusPublicFundsByNinoResponds(200, requestBodyWithRange, responseBodyWithStatus)

  def givenStatusCheckErrorWhenMissingInputField(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoResponds(400, validRequestBody, errorResponseBody)
  }

  def givenStatusCheckErrorWhenStatusNotFound(): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNinoResponds(404, validRequestBody, errorResponseBody)
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

    givenStatusPublicFundsByNinoResponds(422, validRequestBody, errorResponseBody)

  }

  def givenStatusPublicFundsByNinoResponds(
    httpResponseCode: Int,
    requestBody: String,
    responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/status/public-funds/nino"))
        .withHeader("Content-Type", containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

}
