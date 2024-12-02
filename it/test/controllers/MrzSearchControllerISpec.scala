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

import connectors.ErrorCodes._
import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{AUTHORIZATION, await, defaultAwaitTimeout}
import play.api.http.Status._
import stubs.HomeOfficeRightToPublicFundsStubs
import support.{JsonMatchers, ServerBaseISpec}
import play.api.libs.ws.writeableOf_String

import java.util.UUID

class MrzSearchControllerISpec extends ServerBaseISpec with HomeOfficeRightToPublicFundsStubs with JsonMatchers {
  this: Suite & ServerProvider =>

  val url = s"http://localhost:$port"

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def ping: WSResponse = await(wsClient.url(s"$url/ping/ping").get())

  def publicFundsByMrz(payload: String, correlationId: String = "some-correlation-id"): WSResponse =
    await(
      wsClient
        .url(s"$url/v1/status/public-funds/mrz")
        .addHttpHeaders("Content-Type" -> "application/json", AUTHORIZATION -> "Bearer 123")
        .addHttpHeaders((if (correlationId.isEmpty) "" else "x-correlation-id") -> correlationId)
        .post(payload)
    )

  "MrzSearchController" when {

    "POST /v1/status/public-funds/mrz" should {

      "respond with 200 if request is valid" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample(RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)
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
        givenStatusCheckErrorWhenStatusNotFound(RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)

        result.status shouldBe 404
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
        val result        = publicFundsByMrz("{}", correlationId)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed external validation" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid(RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)

        result.status shouldBe 400
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

        val result = publicFundsByMrz("[]")

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("some-correlation-id"))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(400, RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)

        result.status          shouldBe 400
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(404, RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)

        result.status          shouldBe 404
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        ping.status.shouldBe(OK)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(409, RequestType.Mrz)
        givenAuthorisedForStride

        val result = publicFundsByMrz(validMrzRequestBody)

        result.status          shouldBe 409
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json
          .as[JsObject] should haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }
  }
}
