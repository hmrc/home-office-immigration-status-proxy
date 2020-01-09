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

  val requestBodyNoRange: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "RJ301829A"
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
    givenStatusPublicFundsByNino(requestBodyNoRange, responseBodyWithStatus)

  def givenStatusCheckResultWithRangeExample(): StubMapping =
    givenStatusPublicFundsByNino(requestBodyWithRange, responseBodyWithStatus)

  def givenStatusCheckErrorExample(): StubMapping = {

    val responseBody: String =
      """{
        |  "correlationId": "sjdfhks123",
        |  "error": {
        |    "errCode": "ERR_INVALID_REQUEST",
        |    "fields": [
        |      {
        |        "code": "ERR_INVALID_DOB",
        |        "name": "dateOfBirth"
        |      }
        |    ]
        |  }
        |}""".stripMargin

    givenStatusPublicFundsByNino(requestBodyNoRange, responseBody)

  }

  def givenStatusPublicFundsByNino(requestBody: String, responseBody: String): StubMapping =
    stubFor(
      post(urlEqualTo(s"/status/public-funds/nino"))
        .withHeader("Content-Type", containing("application/json"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        ))

}
