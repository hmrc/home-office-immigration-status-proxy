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

import connectors.ErrorCodes.{ERR_NOT_FOUND, ERR_REQUEST_INVALID, ERR_UNKNOWN, ERR_VALIDATION}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{status as playStatus, *}
import stubs.HomeOfficeRightToPublicFundsBaseISpec

import java.util.UUID
import scala.concurrent.Future

class MrzSearchControllerISpec extends HomeOfficeRightToPublicFundsBaseISpec  {
  private val url = "/v1/status/public-funds/mrz"

  private def doPost(payload: String, correlationId: String = "some-correlation-id"): Future[Result] = {
    val hdrs: Seq[Tuple2[String, String]] = Seq(
      "Authorization"    -> "Bearer123",
      "x-correlation-id" -> correlationId
    )

    val request = FakeRequest(POST, url)
      .withHeaders(hdrs*)
      .withJsonBody(Json.parse(payload))
    route(app, request).get
  }

  "MrzSearchController" when {

    "POST /v1/status/public-funds/mrz" should {
      "respond with 200 if request is valid" in {
        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
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
        givenStatusCheckErrorWhenStatusNotFound(RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
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
        val result        = doPost("{}", correlationId)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed external validation" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid(RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
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
        val result = doPost("[]")
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(400, RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
        playStatus(result) shouldBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(404, RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
        playStatus(result) shouldBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(409, RequestType.Mrz)
        givenAuthorisedForStride
        val result = doPost(validMrzRequestBody)
        playStatus(result) shouldBe CONFLICT
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]
        jsonDoc should haveProperty[String]("correlationId")
        jsonDoc should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }
  }
}
