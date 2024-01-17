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

package connectors

import connectors.ErrorCodes._
import connectors.StatusCheckResponseHttpParser._
import models.{StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse, StatusCheckResult}
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

class StatusCheckResponseHttpParserSpec extends AnyWordSpecLike with Matchers {

  val fakeResponseBody = "responseBody"

  "StatusCheckResponseReads.read" should {

    "return a right" when {
      "a 200 is returned with a valid response" in {
        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        val responseBody        = Json.toJson(statusCheckResponse).toString
        val response            = HttpResponse(OK, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)
        result.toOption.get shouldBe statusCheckResponse
      }
    }

    "return a left" when {
      "a 200 is returned without json" in {
        val responseBody = "This is not a valid response"
        val response     = HttpResponse(OK, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode                  shouldBe INTERNAL_SERVER_ERROR
          error.errorResponse.error.errCode shouldBe ERR_HOME_OFFICE_RESPONSE
        }
      }

      "a 200 is returned with an invalid json response" in {
        val responseBody = """{"response": "Something"}"""
        val response     = HttpResponse(OK, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode                  shouldBe INTERNAL_SERVER_ERROR
          error.errorResponse.error.errCode shouldBe ERR_HOME_OFFICE_RESPONSE
        }
      }

      "a 400 is returned with a valid json response" in {
        val responseBody =
          """{"correlationId": "Something", "error": {"errCode": "ERR_SOMETHING"}}"""
        val response = HttpResponse(BAD_REQUEST, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode    shouldBe BAD_REQUEST
          error.errorResponse shouldBe StatusCheckErrorResponse(Some("Something"), StatusCheckError("ERR_SOMETHING"))
        }
      }

      "a 500 is returned with a valid json response" in {
        val responseBody =
          """{"correlationId": "Something", "error": {"errCode": "ERR_SOMETHING"}}"""
        val response = HttpResponse(INTERNAL_SERVER_ERROR, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode    shouldBe INTERNAL_SERVER_ERROR
          error.errorResponse shouldBe StatusCheckErrorResponse(Some("Something"), StatusCheckError("ERR_SOMETHING"))
        }
      }

      "a 400 is returned with an invalid json response" in {
        val responseBody = ""
        val response     = HttpResponse(BAD_REQUEST, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode    shouldBe BAD_REQUEST
          error.errorResponse shouldBe StatusCheckErrorResponse(None, StatusCheckError("ERR_UNKNOWN"))
        }
      }

      "a 500 is returned with an invalid json response" in {
        val responseBody = ""
        val response     = HttpResponse(INTERNAL_SERVER_ERROR, responseBody)

        val result: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
          StatusCheckResponseReads.read("POST", "some url", response)

        inside(result) { case Left(error) =>
          error.statusCode    shouldBe INTERNAL_SERVER_ERROR
          error.errorResponse shouldBe StatusCheckErrorResponse(None, StatusCheckError("ERR_UNKNOWN"))
        }
      }
    }

  }

}
