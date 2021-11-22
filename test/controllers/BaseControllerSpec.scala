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
import play.api.libs.json.{Format, Json, Reads}
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
  }

  object TestController extends BaseController

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

  val successFunction: SomeRequest => Future[Result] = request =>
    Future.successful(Ok("Great success"))

  "withValidParameters" should {
    "return a BadRequest" when {
      "a field is missing" in {
        val correlationId = "correlationId1"
        val request =
          FakeRequest().withBody(Json.parse("""{"intParam":1, "stringParam1":"string"}"""))
        val expectedResult = StatusCheckErrorResponse
          .error(
            Some(correlationId),
            ERR_REQUEST_INVALID,
            Some(List("/stringParam2" -> "error.path.missing")))

        val result = TestController.withValidParameters(correlationId)(successFunction)(
          request,
          implicitly[Reads[SomeRequest]])
        await(result) shouldEqual BadRequest(Json.toJson(expectedResult))
      }

      "a field is invalid" in {
        val correlationId = "correlationId1"
        val request = FakeRequest().withBody(Json
          .parse(
            """{"intParam":"something", "stringParam1": "something", "stringParam2": "something"}"""))
        val expectedResult = StatusCheckErrorResponse
          .error(
            Some(correlationId),
            ERR_REQUEST_INVALID,
            Some(List("/intParam" -> "error.expected.jsnumber")))

        val result = TestController.withValidParameters(correlationId)(successFunction)(
          request,
          implicitly[Reads[SomeRequest]])
        await(result) shouldEqual BadRequest(Json.toJson(expectedResult))
      }
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
