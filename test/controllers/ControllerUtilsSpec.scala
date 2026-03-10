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

import base.SpecBase
import connectors.ErrorCodes.*
import models.{StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse, StatusCheckResult}
import play.api.http.Status.*
import play.api.libs.json.{Format, Json, Reads}
import play.api.mvc.Results.*
import play.api.mvc.*
import play.api.test.FakeRequest

import java.time.LocalDate
import scala.concurrent.Future
import scala.language.postfixOps

class ControllerUtilsSpec extends SpecBase {

  case class SomeRequest(intParam: Int, stringParam1: String, stringParam2: String)
  object SomeRequest {
    implicit val formats: Format[SomeRequest] = Json.format[SomeRequest]
  }

  object TestController extends ControllerUtils

  "eitherToResult" must {

    "return Ok when passed a Right" in {
      val statusCheckResult   = StatusCheckResult("John Doe", LocalDate.now, "GBR", Nil)
      val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
      val response            = Right(statusCheckResponse).withLeft[StatusCheckErrorResponseWithStatus]

      TestController.eitherToResult(response) mustBe Ok(Json.toJson(statusCheckResponse))
    }

    "return NotFound when it's passed a Left with a 404 status" in {
      val statusCheckErrorResponse =
        StatusCheckErrorResponseWithStatus(NOT_FOUND, StatusCheckErrorResponse(None, StatusCheckError("SOMETHING")))
      val response = Left(statusCheckErrorResponse).withRight[StatusCheckResponse]

      TestController.eitherToResult(response) mustBe NotFound(Json.toJson(statusCheckErrorResponse.errorResponse))
    }

    "return BadRequest when it's passed a Left with a 400 status" in {
      val statusCheckErrorResponse =
        StatusCheckErrorResponseWithStatus(BAD_REQUEST, StatusCheckErrorResponse(None, StatusCheckError("SOMETHING")))
      val response = Left(statusCheckErrorResponse).withRight[StatusCheckResponse]

      TestController.eitherToResult(response) mustBe BadRequest(Json.toJson(statusCheckErrorResponse.errorResponse))
    }

    "return InternalServerError when it's passed a Left with a 500 status" in {
      val statusCheckErrorResponse =
        StatusCheckErrorResponseWithStatus(
          INTERNAL_SERVER_ERROR,
          StatusCheckErrorResponse(None, StatusCheckError("SOMETHING"))
        )
      val response = Left(statusCheckErrorResponse).withRight[StatusCheckResponse]

      TestController.eitherToResult(response) mustBe InternalServerError(
        Json.toJson(statusCheckErrorResponse.errorResponse)
      )
    }
  }

  private val successFunction = (request: SomeRequest) => Future.successful(Ok("Great success"))

  "withValidParameters" must {
    "return a BadRequest" when {
      "a json field is missing" in {
        val correlationId = "correlationId1"
        val request =
          FakeRequest().withBody(Json.parse("""{"intParam":1, "stringParam1":"string"}"""))
        val expectedResult = StatusCheckErrorResponse
          .error(Some(correlationId), ERR_REQUEST_INVALID, Some(List("/stringParam2" -> "error.path.missing")))

        val result =
          TestController.withValidParameters(correlationId)(successFunction)(request, implicitly[Reads[SomeRequest]])
        await(result) mustBe BadRequest(Json.toJson(expectedResult))
      }

      "a json field is invalid" in {
        val correlationId = "correlationId1"
        val request = FakeRequest().withBody(
          Json
            .parse("""{"intParam":"something", "stringParam1": "something", "stringParam2": "something"}""")
        )
        val expectedResult = StatusCheckErrorResponse
          .error(Some(correlationId), ERR_REQUEST_INVALID, Some(List("/intParam" -> "error.expected.jsnumber")))

        val result =
          TestController.withValidParameters(correlationId)(successFunction)(request, implicitly[Reads[SomeRequest]])
        await(result) mustBe BadRequest(Json.toJson(expectedResult))
      }
    }

    "call the passed function when a valid json" in {
      val json = Json
        .parse("""{"intParam":1, "stringParam1": "something", "stringParam2": "something"}""")
      val request       = FakeRequest().withBody(json)
      val correlationId = "correlationId1"

      val result =
        TestController.withValidParameters(correlationId)(successFunction)(request, implicitly[Reads[SomeRequest]])
      await(result) mustBe Ok("Great success")
    }
  }

}
