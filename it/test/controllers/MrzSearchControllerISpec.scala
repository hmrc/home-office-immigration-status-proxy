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

package controllers

import connectors.ErrorCodes.{ERR_NOT_FOUND, ERR_REQUEST_INVALID, ERR_UNKNOWN, ERR_VALIDATION}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{status as playStatus, *}
import base.ISpecBase

import java.util.UUID

class MrzSearchControllerISpec extends ISpecBase {
  "MrzSearchController" when {

    "posting to the mrz endpoint" must {
      "respond with 200 if request is valid" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, OK, responseBodyWithStatus)
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe OK
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "result",
            haveProperty[String]("dateOfBirth", be(dob))
              and haveProperty[String]("nationality", be(nationality))
              and haveProperty[String]("fullName", be(s"$fullName"))
              and havePropertyArrayOf[JsObject](
                "statuses",
                haveProperty[String]("productType", be(productType))
                  and haveProperty[String]("immigrationStatus", be(immigrationStatus))
                  and haveProperty[Boolean]("noRecourseToPublicFunds", be(noRecourseToPublicFunds))
                  and haveProperty[String]("statusEndDate", be(statusEndDate))
                  and haveProperty[String]("statusStartDate", be(statusStartDate))
              )
          ))
      }

      "respond with 404 if the service failed to find an identity based on the values provided" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, errorResponseNotFound)
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_NOT_FOUND))
          ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        givenAuthorisedForStride()
        val correlationId = UUID.randomUUID().toString

        val result = post(ninoUrl, "{}", correlationId)

        playStatus(result) mustBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed external validation" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, errorResponseDOBInvalid)
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must (haveProperty[String]("correlationId", be(correlationId))
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
        givenAuthorisedForStride()

        val result = post(mrzUrl, "[]")

        playStatus(result) mustBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, createUnknownErrorResponseUsingStatus(BAD_REQUEST))
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe BAD_REQUEST
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must haveProperty[String]("correlationId")
        jsonDoc must haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, createUnknownErrorResponseUsingStatus(NOT_FOUND))
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe NOT_FOUND
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must haveProperty[String]("correlationId")
        jsonDoc must haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        givenOAuthTokenGranted()
        givenPublicFundsStub(mrzUrl, validMrzRequestBody, createUnknownErrorResponseUsingStatus(CONFLICT))
        givenAuthorisedForStride()

        val result = post(mrzUrl, validMrzRequestBody)

        playStatus(result) mustBe CONFLICT
        val jsonDoc = Json.parse(contentAsString(result)).as[JsObject]

        jsonDoc must haveProperty[String]("correlationId")
        jsonDoc must haveProperty[JsObject]("error", haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }
  }
}
