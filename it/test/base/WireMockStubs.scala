/*
 * Copyright 2026 HM Revenue & Customs
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

package base

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.TestData
import models.{ImmigrationStatus, StatusCheckResponse, StatusCheckResult}
import org.scalatest.Suite
import play.api.http.Status.{BAD_REQUEST, CONFLICT, NOT_FOUND, OK}
import play.mvc.Http.HeaderNames
import support.WireMockSupport
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.time.LocalDate

trait WireMockStubs extends WireMockSupport with TestData {
  me: Suite =>

  protected val ninoRequestBodyWithRange: String =
    s"""{
      |  "dateOfBirth": "$dob",
      |  "familyName": "$familyName",
      |  "givenName": "$givenName",
      |  "nino": "$nino",
      |  "statusCheckRange": {
      |    "endDate": "$rangeEndDate",
      |    "startDate": "$rangeStartDate"
      |  }
      |}""".stripMargin

  protected val mrzRequestBodyWithRange: String =
    s"""{
      |  "dateOfBirth": "$dob",
      |  "documentNumber": "$documentNumber",
      |  "documentType": "$documentType",
      |  "nationality": "$nationality",
      |  "statusCheckRange": {
      |    "endDate": "$rangeEndDate",
      |    "startDate": "$rangeStartDate"
      |  }
      |}""".stripMargin

  protected val validNinoRequestBody: String =
    s"""{
      |  "dateOfBirth": "$dob",
      |  "familyName": "$familyName",
      |  "givenName": "$givenName",
      |  "nino": "$nino"
      |}""".stripMargin

  protected val invalidNinoRequestBody: String =
    s"""{
      |  "dateOfBirth": "$dob",
      |  "familyName": "$familyName",
      |  "givenName": "$givenName",
      |  "nino": "invalid"
      |}""".stripMargin

  protected val validMrzRequestBody: String =
    s"""{
      |  "dateOfBirth": "$dob",
      |  "documentNumber": "$documentNumber",
      |  "documentType": "$documentType",
      |  "nationality": "$nationality"
      |}""".stripMargin

  protected val responseBodyWithStatus: String =
    s"""{
      |  "correlationId": "$correlationId",
      |  "result": {
      |    "dateOfBirth": "$dob",
      |    "nationality": "$nationality",
      |    "fullName": "$fullName",
      |    "statuses": [
      |      {
      |        "productType": "$productType",
      |        "immigrationStatus": "$immigrationStatus",
      |        "noRecourseToPublicFunds": $noRecourseToPublicFunds,
      |        "statusEndDate": "$statusEndDate",
      |        "statusStartDate": "$statusStartDate"
      |      }
      |    ]
      |  }
      |}""".stripMargin

  protected val responseBodyWithStatusObject: StatusCheckResponse = StatusCheckResponse(
    correlationId = correlationId,
    result = StatusCheckResult(
      dateOfBirth = LocalDate.parse(dob),
      nationality = nationality,
      fullName = fullName,
      statuses = List(
        ImmigrationStatus(
          productType = productType,
          immigrationStatus = immigrationStatus,
          noRecourseToPublicFunds = noRecourseToPublicFunds,
          statusEndDate = Some(LocalDate.parse(statusEndDate)),
          statusStartDate = LocalDate.parse(statusStartDate)
        )
      )
    )
  )

  protected val errorResponseBadRequest: ErrorResponse = ErrorResponse(
    BAD_REQUEST,
    s"""{
        |  "correlationId": "$correlationId",
        |  "error": {
        |    "errCode": "ERR_REQUEST_INVALID"
        |  }
        |}""".stripMargin
  )

  protected val errorResponseNotFound: ErrorResponse =
    ErrorResponse(
      NOT_FOUND,
      s"""{
        |  "correlationId": "$correlationId",
        |  "error": {
        |    "errCode": "ERR_NOT_FOUND"
        |  }
        |}""".stripMargin
    )

  protected val errorResponseConflict: ErrorResponse = ErrorResponse(
    CONFLICT,
    s"""{
        |  "correlationId": "$correlationId",
        |  "error": {
        |    "errCode": "ERR_CONFLICT"
        |  }
        |}""".stripMargin
  )

  protected val errorResponseDOBInvalid: ErrorResponse =
    ErrorResponse(
      BAD_REQUEST,
      s"""{
        |  "correlationId": "$correlationId",
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
    )

  protected def createUnknownErrorResponseUsingStatus(status: Int): ErrorResponse =
    ErrorResponse(
      status,
      s"""{
        |  "correlationId": "$correlationId",
        |}""".stripMargin
    )

  protected def givenOAuthTokenGranted(): StubMapping = {
    val oAuthTokenResponse: String =
      s"""{
        |   "access_token": "$tokenId",
        |   "refresh_token": "not-used",
        |   "token_type": "$tokenType"
        |}""".stripMargin
    wireMockStubForToken(OK, validTokenForm, oAuthTokenResponse)
  }

  protected def givenOAuthTokenGrantedWithoutRefresh(): StubMapping = {
    val oAuthTokenResponse: String =
      s"""{
        |   "access_token": "$tokenId",
        |   "token_type": "$tokenType"
        |}""".stripMargin
    wireMockStubForToken(OK, validTokenForm, oAuthTokenResponse)
  }

  protected def givenOAuthTokenDenied(): StubMapping = wireMockStubForToken(401, validTokenForm, "")

  private def wireMockStubForToken(httpResponseCode: Int, requestBody: String, responseBody: String): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo(tokenUrl))
        .withHeader("X-Correlation-Id", equalTo(correlationId))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/x-www-form-urlencoded"))
        .withRequestBody(equalTo(requestBody))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )

  protected def givenPublicFundsStub(
    url: String,
    requestBody: String,
    httpResponseCode: Int,
    responseBody: String
  ): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo(url))
        .withHeader("X-Correlation-Id", equalTo(correlationId))
        .withHeader(HeaderNames.CONTENT_TYPE, containing("application/json"))
        .withHeader(HeaderNames.AUTHORIZATION, containing(s"$tokenType $tokenId"))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse()
            .withStatus(httpResponseCode)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Correlation-Id", correlationId)
            .withBody(responseBody)
        )
    )

  protected def givenPublicFundsStub(url: String, requestBody: String, errorResponse: ErrorResponse): StubMapping =
    givenPublicFundsStub(url, requestBody, errorResponse.statusCode, errorResponse.message)

  protected def givenAuthorisedForInternalAuth(): StubMapping =
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

  protected def givenAuthorisedForStride(): StubMapping =
    wireMockServer.stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(1)
        .withRequestBody(
          equalToJson(
            """
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
