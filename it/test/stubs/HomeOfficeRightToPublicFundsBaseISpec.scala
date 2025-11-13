/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.{ImmigrationStatus, StatusCheckResponse, StatusCheckResult}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, CONFLICT, NOT_FOUND, OK, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames
import support.{JsonMatchers, WireMockSupport}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate

trait HomeOfficeRightToPublicFundsBaseISpec  extends AnyWordSpecLike
  with Matchers
  with OptionValues
  with WireMockSupport
  with JsonMatchers
  with ScalaFutures
  with IntegrationPatience {
  me: WireMockSupport =>
  
  protected lazy val app: Application = appBuilder.build()

  private def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.home-office-right-to-public-funds.port" -> wireMockServer.port(),
        "microservice.services.internal-auth.port" -> wireMockServer.port(),
        "microservice.services.auth.port" -> wireMockServer.port(),
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.port" -> wireMockServer.port()
      )

  private val validTokenForm = """grant_type=client_credentials&client_id=hmrc&client_secret=TBC"""

  private val ninoRequestBodyWithRange: String =
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

  private val mrzRequestBodyWithRange: String =
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

  protected val validNinoRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "RJ301829A"
      |}""".stripMargin

  protected val invalidNinoRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "familyName": "Jane",
      |  "givenName": "Doe",
      |  "nino": "invailid"
      |}""".stripMargin

  protected val validMrzRequestBody: String =
    """{
      |  "dateOfBirth": "2001-01-31",
      |  "documentNumber": "1234567890",
      |  "documentType": "PASSPORT",
      |  "nationality": "USA"
      |}""".stripMargin

  private val responseBodyWithStatus: String =
    """{
      |  "correlationId": "some-correlation-id",
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

  protected val responseBodyWithStatusObject: StatusCheckResponse = StatusCheckResponse(
    correlationId = "some-correlation-id",
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

  protected sealed trait RequestType

  protected object RequestType {
    case object Mrz extends RequestType
    case object Nino extends RequestType
  }

  private def getValidRequest(requestType: RequestType): String = requestType match {
    case RequestType.Nino => validNinoRequestBody
    case RequestType.Mrz => validMrzRequestBody
  }

  private def getRequestWithRange(requestType: RequestType): String = requestType match {
    case RequestType.Nino => ninoRequestBodyWithRange
    case RequestType.Mrz => mrzRequestBodyWithRange
  }

  protected def givenStatusCheckResultNoRangeExample(requestType: RequestType): StubMapping =
    givenSearchStub(requestType, OK, getValidRequest(requestType), responseBodyWithStatus)

  protected def givenStatusCheckResultWithRangeExample(requestType: RequestType): StubMapping =
    givenSearchStub(requestType, OK, getRequestWithRange(requestType), responseBodyWithStatus)

  protected def givenStatusCheckErrorWhenMissingInputField(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "some-correlation-id",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, BAD_REQUEST, getValidRequest(requestType), errorResponseBody)
  }

  protected def givenStatusCheckErrorWhenStatusNotFound(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "some-correlation-id",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, NOT_FOUND, getValidRequest(requestType), errorResponseBody)
  }

  protected def givenStatusCheckErrorUndefined(status: Int, requestType: RequestType): StubMapping = {

    assert(status >= 400 && status < 500)

    val errorResponseBody: String =
      """{
        |  "correlationId": "some-correlation-id"
        |}""".stripMargin

    givenSearchStub(requestType, status, getValidRequest(requestType), errorResponseBody)
  }

  protected def givenStatusCheckErrorWhenConflict(requestType: RequestType): StubMapping = {

    val errorResponseBody: String =
      """{
        |  "correlationId": "some-correlation-id",
        |  "error": {
        |    "errCode": "ERR_CONFLICT"
        |  }
        |}""".stripMargin

    givenSearchStub(requestType, CONFLICT, getValidRequest(requestType), errorResponseBody)

  }

  protected def givenStatusCheckErrorWhenDOBInvalid(requestType: RequestType): StubMapping = {
    val errorResponseBody: String =
      """{
        |  "correlationId": "some-correlation-id",
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

    givenSearchStub(requestType, BAD_REQUEST, getValidRequest(requestType), errorResponseBody)
  }

  protected def givenOAuthTokenGranted(): StubMapping = {
    val oAuthTokenResponse: String =
      """{
        |   "access_token": "FOO0123456789",
        |   "refresh_token": "not-used",
        |   "token_type": "SomeTokenType"
        |}""".stripMargin
    wireMockStubForToken(OK, validTokenForm, oAuthTokenResponse)
  }

  protected def givenOAuthTokenGrantedWithoutRefresh(): StubMapping = {
    val oAuthTokenResponse: String =
      """{
        |   "access_token": "FOO0123456789",
        |   "token_type": "SomeTokenType"
        |}""".stripMargin
    wireMockStubForToken(OK, validTokenForm, oAuthTokenResponse)
  }

  protected def givenOAuthTokenDenied(): StubMapping = wireMockStubForToken(401, validTokenForm, "")


 

  def givenSearchStub(
                       requestType: RequestType,
                       httpResponseCode: Int,
                       requestBody: String,
                       responseBody: String
                     ): StubMapping =
    requestType match {
      case RequestType.Mrz => wireMockStubForMrz(httpResponseCode, requestBody, responseBody)
      case RequestType.Nino => wireMockStubForNino(httpResponseCode, requestBody, responseBody)
    }

  protected def wireMockStubForInternalAuthSuccessful(): StubMapping = 
    wireMockServer.stubFor(
    post(urlEqualTo("/internal-auth/auth"))
      .willReturn(
        okJson(
          """{
                "retrievals": []
              }"""
        )
      )
  )

  private def wireMockStubForToken(httpResponseCode: Int, requestBody: String, responseBody: String): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/token"))
        .withHeader("X-Correlation-Id", equalTo("some-correlation-id"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/x-www-form-urlencoded"))
        .withRequestBody(equalTo(requestBody))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )

  protected def wireMockStubForNino(httpResponseCode: Int, requestBody: String, responseBody: String): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo("/v1/status/public-funds/nino"))
        .withHeader("X-Correlation-Id", equalTo("some-correlation-id"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withHeader(HeaderNames.AUTHORIZATION, containing("SomeTokenType FOO0123456789"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Correlation-Id", "some-correlation-id")
            .withBody(responseBody)
        )
    )

  protected def wireMockStubForMrz(httpResponseCode: Int, requestBody: String, responseBody: String): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo(s"/v1/status/public-funds/mrz"))
        .withHeader("X-Correlation-Id", equalTo("some-correlation-id"))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withHeader(HeaderNames.AUTHORIZATION, containing("SomeTokenType FOO0123456789"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Correlation-Id", "some-correlation-id")
            .withBody(responseBody)
        )
    )

  protected def wireMockStubForStride: StubMapping = {
    wireMockServer.stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(1)
        .withRequestBody(
          equalToJson(
            s"""
               |{
               |  "authorise": [
               |    {
               |      "authProviders": [
               |        "PrivilegedApplication"
               |      ]
               |    }
               |  ]
               |}
           """.stripMargin,
            true,
            true
          )
        )
        .willReturn(
          aResponse()
            .withBody("{}")
            .withStatus(OK)
        )
    )

  }
}
