/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import models.{StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse, StatusCheckResult}
import play.api.http.Status._
import play.api.mvc._
import play.api.mvc.Results._
import java.time.LocalDate
import play.api.libs.json.{Format, JsNull, JsObject, Json, Reads}
import scala.concurrent.Future
import connectors.ErrorCodes._
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}
import scala.language.postfixOps

class BaseControllerSpec extends AnyWordSpecLike with Matchers {

  val timeoutDuration: FiniteDuration = 5 seconds
  def await[T](future: Awaitable[T]): T = Await.result(future, timeoutDuration)

  case class SomeRequest(intParam: Int, stringParam1: String, stringParam2: String)
  object SomeRequest {
    implicit val formats: Format[SomeRequest] = Json.format[SomeRequest]
    val mandatoryFields: Set[String] =
      Set("intParam", "stringParam1", "stringParam2")
  }

  object TestController extends BaseController {
    val mandatoryFields = SomeRequest.mandatoryFields
  }

  "eitherToResult" should {

    "return Ok when it's a right" in {
      val statusCheckResult = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
      val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
      val either: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        Right(statusCheckResponse)
      TestController.eitherToResult(either) shouldEqual Ok(Json.toJson(statusCheckResponse))
    }

    "return NotFound when it's a left with a 404 status" in {
      val response =
        StatusCheckErrorResponseWithStatus(
          NOT_FOUND,
          StatusCheckErrorResponse(None, StatusCheckError("SOMETHING")))
      val either: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        Left(response)
      TestController.eitherToResult(either) shouldEqual NotFound(
        Json.toJson(response.errorResponse))
    }

    "return BadRequest when it's a left with a 400 status" in {
      val response =
        StatusCheckErrorResponseWithStatus(
          BAD_REQUEST,
          StatusCheckErrorResponse(None, StatusCheckError("SOMETHING")))
      val either: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        Left(response)
      TestController.eitherToResult(either) shouldEqual BadRequest(
        Json.toJson(response.errorResponse))
    }

    "return InternalServerError when it's a left with a 500 status" in {
      val response =
        StatusCheckErrorResponseWithStatus(
          INTERNAL_SERVER_ERROR,
          StatusCheckErrorResponse(None, StatusCheckError("SOMETHING")))
      val either: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
        Left(response)
      TestController.eitherToResult(either) shouldEqual InternalServerError(
        Json.toJson(response.errorResponse))
    }

  }

  "validateJson" should {
    "return a MissingInputFields" when {
      "all fields are missing" in {
        val json = Json.parse("""{"differentParam":"something"}""").as[JsObject]
        TestController
          .validateJson[SomeRequest](json) shouldEqual MissingInputFields(
          SomeRequest.mandatoryFields.toList)
      }

      "one field is missing" in {
        val json = Json.parse("""{"intParam":1, "stringParam1":"string"}""").as[JsObject]
        TestController
          .validateJson[SomeRequest](json) shouldEqual MissingInputFields(List("stringParam2"))
      }
    }

    "return a InvalidInputFields" when {
      "all fields are invalid" in {
        val json = Json
          .parse("""{"intParam":"something", "stringParam1": 2, "stringParam2": 3}""")
          .as[JsObject]
        val result = TestController
          .validateJson[SomeRequest](json)
        result shouldEqual InvalidInputFields(
          List(
            "/intParam"     -> "error.expected.jsnumber",
            "/stringParam1" -> "error.expected.jsstring",
            "/stringParam2" -> "error.expected.jsstring"))
      }

      "one field is invalid" in {
        val json = Json
          .parse(
            """{"intParam":"something", "stringParam1": "something", "stringParam2": "something"}""")
          .as[JsObject]
        val result = TestController
          .validateJson[SomeRequest](json)
        result shouldEqual InvalidInputFields(List("/intParam" -> "error.expected.jsnumber"))
      }
    }

    "return a ValidRequest" in {
      val json = Json
        .parse("""{"intParam":1, "stringParam1": "something", "stringParam2": "something"}""")
        .as[JsObject]
      val result = TestController
        .validateJson[SomeRequest](json)
      result shouldEqual ValidRequest(SomeRequest(1, "something", "something"))
    }
  }

  val successFunction: SomeRequest => Future[Result] = request =>
    Future.successful(Ok("Great success"))

  "validate" should {
    "return a BadRequest" when {
      "a field is missing" in {
        val correlationId = "correlationId1"
        val json = Json.parse("""{"intParam":1, "stringParam1":"string"}""").as[JsObject]
        val expectedResult = StatusCheckErrorResponse
          .error(Some(correlationId), ERR_REQUEST_INVALID, Some(List("/stringParam2" -> "missing")))

        val result = TestController.validate(json, successFunction, correlationId)
        await(result) shouldEqual BadRequest(Json.toJson(expectedResult))
      }

      "a field is invalid" in {
        val correlationId = "correlationId1"
        val json = Json
          .parse(
            """{"intParam":"something", "stringParam1": "something", "stringParam2": "something"}""")
          .as[JsObject]
        val expectedResult = StatusCheckErrorResponse
          .error(
            Some(correlationId),
            ERR_VALIDATION,
            Some(List("/intParam" -> "error.expected.jsnumber")))

        val result = TestController.validate(json, successFunction, correlationId)
        await(result) shouldEqual BadRequest(Json.toJson(expectedResult))
      }
    }

    "call our function with a valid object" in {
      val correlationId = "correlationId1"
      val json = Json
        .parse("""{"intParam":1, "stringParam1": "something", "stringParam2": "something"}""")
        .as[JsObject]

      val result = TestController.validate(json, successFunction, correlationId)
      await(result) shouldEqual Ok("Great success")
    }

  }

  "withValidParameters" should {
    "return a BadRequest when we don't have valid json" in {
      val request = FakeRequest().withBody(JsNull)
      val correlationId = "correlationId1"
      val expectedResult = StatusCheckErrorResponse.error(Some(correlationId), ERR_REQUEST_INVALID)

      val result =
        TestController.withValidParameters(correlationId)(successFunction)(
          request,
          implicitly[Reads[SomeRequest]])
      await(result) shouldEqual BadRequest(Json.toJson(expectedResult))
    }

    "call our function with a valid object" in {
      val json = Json
        .parse("""{"intParam":1, "stringParam1": "something", "stringParam2": "something"}""")
      val request = FakeRequest().withBody(json)
      val correlationId = "correlationId1"

      val result =
        TestController.withValidParameters(correlationId)(successFunction)(
          request,
          implicitly[Reads[SomeRequest]])
      await(result) shouldEqual Ok("Great success")
    }
  }

}
