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

package controllers

import com.github.tomakehurst.wiremock.client.WireMock.status as _
import connectors.ErrorCodes.{ERR_NOT_FOUND, ERR_REQUEST_INVALID, ERR_UNKNOWN, ERR_VALIDATION}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{status as playStatus, *}
import stubs.HomeOfficeRightToPublicFundsBaseISpec

import java.util.UUID
import scala.concurrent.Future

class NinoSearchControllerISpec extends HomeOfficeRightToPublicFundsBaseISpec {
  private val urlWithoutClientService = "/v1/status/public-funds/nino"
  private val urlWithClientService    = s"/v1/status/public-funds/nino/service-a"

  private def doPostWithoutClientService(payload: String, correlationId: String = "some-correlation-id"): Future[Result] = {
    val hdrs: Seq[Tuple2[String, String]] = Seq(
      "Authorization"    -> "Bearer123",
      "x-correlation-id" -> correlationId
    )

    val request = FakeRequest(POST, urlWithoutClientService)
      .withHeaders(hdrs*)
      .withJsonBody(Json.parse(payload))
    route(app, request).get
  }
  private def doPostWithClientService(
    payload: String,
    correlationId: String = "some-correlation-id"
  ): Future[Result] = {
    val hdrs: Seq[Tuple2[String, String]] = Seq(
      "Authorization"    -> "Bearer123",
      "x-correlation-id" -> correlationId
    )

    val request = FakeRequest(POST, urlWithClientService)
      .withHeaders(hdrs*)
      .withJsonBody(Json.parse(payload))
    route(app, request).get
  }

  override def beforeEach(): Unit = super.beforeEach()

  "NinoSearchController" when {

    "POST /v1/status/public-funds/nino" should {
      "respond with 200 if request is valid" in {
        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Nino)
        givenAuthorisedForStride
        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe OK
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenStatusNotFound(RequestType.Nino)
        givenAuthorisedForStride

        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_NOT_FOUND))
          ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        givenAuthorisedForStride
        val correlationId = UUID.randomUUID().toString
        val result        = doPostWithoutClientService("{}", correlationId)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed internal validation" in {
        givenAuthorisedForStride
        val result = doPostWithoutClientService(invalidNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid(RequestType.Nino)
        givenAuthorisedForStride
        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenOAuthTokenGranted()
        givenAuthorisedForStride
        val result = doPostWithoutClientService("[]")
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(BAD_REQUEST, RequestType.Nino)
        givenAuthorisedForStride

        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(NOT_FOUND, RequestType.Nino)
        givenAuthorisedForStride

        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(CONFLICT, RequestType.Nino)
        givenAuthorisedForStride
        val result = doPostWithoutClientService(validNinoRequestBody)
        playStatus(result) shouldBe CONFLICT
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }

    "POST /v1/status/public-funds/nino/:service" should {
      "respond with 200 if request is valid" in {
        givenAuthorisedForInternalAuth()
        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Nino)
        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe OK
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenAuthorisedForInternalAuth()
        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenStatusNotFound(RequestType.Nino)
        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_NOT_FOUND))
          ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        givenAuthorisedForInternalAuth()

        val correlationId = UUID.randomUUID().toString

        val result = doPostWithClientService("{}", correlationId)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed internal validation" in {
        givenAuthorisedForInternalAuth()

        val result = doPostWithClientService(invalidNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenAuthorisedForInternalAuth()

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid(RequestType.Nino)

        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
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
        givenAuthorisedForInternalAuth()

        givenOAuthTokenGranted()
        val result = doPostWithClientService("[]")
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        givenAuthorisedForInternalAuth()

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(BAD_REQUEST, RequestType.Nino)
        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        givenAuthorisedForInternalAuth()

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(NOT_FOUND, RequestType.Nino)
        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        givenAuthorisedForInternalAuth()

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(CONFLICT, RequestType.Nino)

        val result = doPostWithClientService(validNinoRequestBody)
        playStatus(result) shouldBe CONFLICT
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }
  }
}
