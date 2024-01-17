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

import cats.implicits._
import connectors.ErrorCodes._
import models._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.test.FakeRequest

import java.time.LocalDate

class MrzSearchControllerSpec extends ControllerSpec {

  val controller: MrzSearchController = inject[MrzSearchController]

  val req: StatusCheckByMrzRequest = (
    DocumentNumber("1234567890"),
    DateOfBirth(LocalDate.now.minusDays(1)),
    Nationality("USA")
  ).mapN((docNumber, dob, nat) => StatusCheckByMrzRequest(DocumentType.Passport, docNumber, dob, nat)).toOption.get

  "post" should {

    val requestBody = Json.toJson(req)
    val request: FakeRequest[JsValue] =
      FakeRequest().withBody(requestBody).withHeaders("X-Correlation-Id" -> correlationId)

    "fail" when {

      "connector.token fails" in {
        tokenCallFails
        intercept[Exception](await(controller.post(request)))
      }

      "connector.token is successful but connector.statusPublicFundsByMrz fails" in {
        tokenCallIsSuccessful
        requestMrzCallFails
        intercept[Exception](await(controller.post(request)))
      }

    }

    "return 200" when {

      "the connector calls are successful and the validation passes" in {
        tokenCallIsSuccessful
        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestMrzCallIsSuccessful(Right(statusCheckResponse))
        await(controller.post(request)) mustEqual withHeaders(Ok(Json.toJson(statusCheckResponse)))
      }

      "a request is made without a correlationId but is successful" in {
        tokenCallIsSuccessful
        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestMrzCallIsSuccessful(Right(statusCheckResponse))
        val requestWithoutCorrelationId: FakeRequest[JsValue] =
          FakeRequest().withBody(requestBody)
        requestWithoutCorrelationId.headers.headers.find(_._1 == "X-Correlation-Id") must be(None)
        await(controller.post(requestWithoutCorrelationId)).header.headers
          .find(_._1 == "X-Correlation-Id") must not be empty
      }

    }

    "return an error" when {

      "the validation passes but the connector returns an internal error status" in {
        tokenCallIsSuccessful
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(INTERNAL_SERVER_ERROR, errorResponse)
        requestMrzCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(InternalServerError(Json.toJson(errorResponse)))
      }

      "the validation passes but the connector returns a bad request status" in {
        tokenCallIsSuccessful
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(BAD_REQUEST, errorResponse)
        requestMrzCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(BadRequest(Json.toJson(errorResponse)))
      }

    }

  }

}
