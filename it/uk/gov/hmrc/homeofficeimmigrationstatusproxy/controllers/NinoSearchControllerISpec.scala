/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import connectors.ErrorCodes._
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.ServerProvider
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import stubs.HomeOfficeRightToPublicFundsStubs
import support.{JsonMatchers, ServerBaseISpec}

import java.util.UUID

class NinoSearchControllerISpec extends ServerBaseISpec with HomeOfficeRightToPublicFundsStubs with JsonMatchers {
    this: Suite with ServerProvider =>

  val url = s"http://localhost:$port"

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def ping: WSResponse = wsClient.url(s"$url/ping/ping").get().futureValue

  private val internalAuthBaseUrl: String = "http://localhost:8470"
  private val clientAuthToken: String = UUID.randomUUID().toString

  private val clientService: String = "service-a"
  def publicFundsByNinoForService(service: String, payload: String, correlationId: String = "some-correlation-id"): WSResponse =
    wsClient
      .url(s"$url/v1/status/public-funds/nino/$service")
      .addHttpHeaders("Content-Type" -> "application/json", AUTHORIZATION -> clientAuthToken)
      .addHttpHeaders((if (correlationId.isEmpty) "" else "x-correlation-id") -> correlationId)
      .post(payload)
      .futureValue

  def publicFundsByNino(payload: String, correlationId: String = "some-correlation-id"): WSResponse =
    wsClient
      .url(s"$url/v1/status/public-funds/nino")
      .addHttpHeaders("Content-Type" -> "application/json", AUTHORIZATION -> "Bearer 123")
      .addHttpHeaders((if (correlationId.isEmpty) "" else "x-correlation-id") -> correlationId)
      .post(payload)
      .futureValue

  private def createClientAuthToken(): Unit = {
    val response = wsClient.url(s"$internalAuthBaseUrl/test-only/token")
      .post(
        Json.obj(
          "token" -> clientAuthToken,
          "principal" -> "test",
          "permissions" -> Seq(
            Json.obj(
              "resourceType" -> "home-office-immigration-status-proxy",
              "resourceLocation" -> s"status/public-funds/nino/$clientService",
              "actions" -> List("WRITE")
            )
          )
        )
      ).futureValue
    response.status shouldBe CREATED
  }

  private def authTokenIsValid(token: String): Boolean = {
    val response = wsClient.url(s"$internalAuthBaseUrl/test-only/token")
      .withHttpHeaders("Authorization" -> token)
      .get()
      .futureValue
    response.status == OK
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    if (!authTokenIsValid(clientAuthToken)) createClientAuthToken()
  }

  "NinoSearchController" when {

    "POST /v1/status/public-funds/nino" should {

      "respond with 200 if request is valid" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)
        result.status shouldBe OK
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
          "result",
          haveProperty[String]("dateOfBirth", be("2001-01-31"))
            and haveProperty[String]("nationality", be("IRL"))
            and haveProperty[String]("fullName", be("Jane Doe"))
            and havePropertyArrayOf[JsObject](
            "statuses",
            haveProperty[String]("productType", be("EUS"))
              and haveProperty[String]("immigrationStatus", be("ILR"))
              and haveProperty[Boolean]("noRecourseToPublicFunds", be(true))
              and haveProperty[String]("statusEndDate", be("2018-01-31"))
              and haveProperty[String]("statusStartDate", be("2018-12-12"))
          )
        ))
      }

      "respond with 404 if the service failed to find an identity based on the values provided" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenStatusNotFound(RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)

        result.status shouldBe NOT_FOUND
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_NOT_FOUND))
        ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        ping.status.shouldBe(OK)

        givenAuthorisedForStride

        val correlationId = UUID.randomUUID().toString
        val result = publicFundsByNino("{}", correlationId)

        result.status shouldBe BAD_REQUEST
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
        ))
      }

      "respond with 400 if one of the input parameters passed in has failed internal validation" in {
        ping.status.shouldBe(OK)

        givenAuthorisedForStride

        val result = publicFundsByNino(invalidNinoRequestBody)

        result.status shouldBe BAD_REQUEST
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
            and havePropertyArrayOf[JsObject](
            "fields",
            haveProperty[String]("code")
              and haveProperty[String]("name")
          )
        ))
      }

      "respond with 400 if one of the input parameters passed in has failed external validation" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid(RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)

        result.status shouldBe BAD_REQUEST
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_VALIDATION))
            and havePropertyArrayOf[JsObject](
            "fields",
            haveProperty[String]("code")
              and haveProperty[String]("name")
          )
        ))
      }

      "respond with 400 if request payload is invalid json" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenAuthorisedForStride

        val result = publicFundsByNino("[]")

        result.status shouldBe BAD_REQUEST
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(BAD_REQUEST, RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)

        result.status shouldBe BAD_REQUEST
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(NOT_FOUND, RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)

        result.status shouldBe NOT_FOUND
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(CONFLICT, RequestType.Nino)
        givenAuthorisedForStride

        val result = publicFundsByNino(validNinoRequestBody)

        result.status shouldBe CONFLICT
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }

    "POST /v1/status/public-funds/nino/:service" should {

      "respond with 200 if request is valid" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Nino)

        val result = publicFundsByNinoForService(clientService, validNinoRequestBody)

        result.status shouldBe OK
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
          "result",
          haveProperty[String]("dateOfBirth", be("2001-01-31"))
            and haveProperty[String]("nationality", be("IRL"))
            and haveProperty[String]("fullName", be("Jane Doe"))
            and havePropertyArrayOf[JsObject](
            "statuses",
            haveProperty[String]("productType", be("EUS"))
              and haveProperty[String]("immigrationStatus", be("ILR"))
              and haveProperty[Boolean]("noRecourseToPublicFunds", be(true))
              and haveProperty[String]("statusEndDate", be("2018-01-31"))
              and haveProperty[String]("statusStartDate", be("2018-12-12"))
          )
        ))
      }
    }
  }
}