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

import connectors.ErrorCodes.*
import models.*
import org.mockito.ArgumentMatchers.{any, eq as mEq}
import org.mockito.Mockito.when
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.*
import play.api.test.FakeRequest
import play.api.test.Helpers.AUTHORIZATION
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.internalauth.client.*

import java.time.LocalDate
import scala.concurrent.Future

class NinoSearchControllerSpec extends ControllerSpec {

  private lazy val controller: NinoSearchController = inject[NinoSearchController]

  private val req = DateOfBirth(LocalDate.now.minusDays(1))
    .map(StatusCheckByNinoRequest(_, "Jane", "Doe", Nino("RJ301829A")))
    .toOption
    .get
  private val requestBody = Json.toJson(req)
  private val request: FakeRequest[JsValue] =
    FakeRequest().withBody(requestBody).withHeaders("X-Correlation-Id" -> correlationId)

  "post" should {

    "return 200" when {

      "the connector calls are successful and the validation passes" in {
        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestNinoCallIsSuccessful(Right(statusCheckResponse))
        await(controller.post(request)) mustEqual withHeaders(Ok(Json.toJson(statusCheckResponse)))
      }

      "a request is made without a correlationId but is successful" in {
        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestNinoCallIsSuccessful(Right(statusCheckResponse))
        val requestWithoutCorrelationId: FakeRequest[JsValue] =
          FakeRequest().withBody(requestBody)
        requestWithoutCorrelationId.headers.headers.find(_._1 == "X-Correlation-Id") must be(None)
        await(controller.post(requestWithoutCorrelationId)).header.headers
          .find(_._1 == "X-Correlation-Id") must not be empty
      }
    }

    "return an error" when {

      "the validation passes but the connector returns an internal error status" in {
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(INTERNAL_SERVER_ERROR, errorResponse)
        requestNinoCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(InternalServerError(Json.toJson(errorResponse)))
      }

      "the validation passes but the connector returns a bad request status" in {
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(BAD_REQUEST, errorResponse)
        requestNinoCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(BadRequest(Json.toJson(errorResponse)))
      }

    }

  }

  "postByService" should {

    val service: String  = "service-a"
    val action: IAAction = IAAction("WRITE")

    val expectedPredicate: Predicate.Permission = Predicate.Permission(
      resource = Resource(
        resourceType = ResourceType("home-office-immigration-status-proxy"),
        resourceLocation = ResourceLocation(s"status/public-funds/nino/$service")
      ),
      action = action
    )

    "fail" when {
      Seq(
        UpstreamErrorResponse("Unauthorized", UNAUTHORIZED),
        UpstreamErrorResponse("Forbidden", FORBIDDEN)
      ).foreach { response =>
        s"internalAuthAction.authorizedAction fails and returns ${response.statusCode}" in {

          when(mockStubBehaviour.stubAuth[Unit](mEq(Some(expectedPredicate)), any()))
            .thenReturn(Future.failed(response))

          val requestWithToken = request.withHeaders(AUTHORIZATION -> "token")

          intercept[Exception](await(controller.postByService("service-a")(requestWithToken)))
        }
      }
    }

    "return 200" when {

      "the connector calls are successful and the validation passes" in {

        when(mockStubBehaviour.stubAuth[Unit](mEq(Some(expectedPredicate)), any())).thenReturn(Future.unit)

        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestNinoCallIsSuccessful(Right(statusCheckResponse))

        val requestWithToken = request.withHeaders(AUTHORIZATION -> "token")

        await(controller.postByService("service-a")(requestWithToken)) mustEqual withHeaders(
          Ok(Json.toJson(statusCheckResponse))
        )
      }

      "a request is made without a correlationId but is successful" in {

        when(mockStubBehaviour.stubAuth[Unit](mEq(Some(expectedPredicate)), any())).thenReturn(Future.unit)

        val statusCheckResult   = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestNinoCallIsSuccessful(Right(statusCheckResponse))
        val requestWithoutCorrelationId: FakeRequest[JsValue] =
          FakeRequest().withBody(requestBody).withHeaders(AUTHORIZATION -> "token")

        requestWithoutCorrelationId.headers.headers.find(_._1 == "X-Correlation-Id") must be(None)
        await(controller.postByService("service-a")(requestWithoutCorrelationId)).header.headers
          .find(_._1 == "X-Correlation-Id") must not be empty
      }
    }

    "return an error" when {

      "the validation passes but the connector returns an internal error status" in {
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(INTERNAL_SERVER_ERROR, errorResponse)
        requestNinoCallIsSuccessful(Left(errorResponseWithStatus))

        val requestWithToken = request.withHeaders(AUTHORIZATION -> "token")

        await(controller.postByService("service-a")(requestWithToken)) mustEqual withHeaders(
          InternalServerError(Json.toJson(errorResponse))
        )
      }

      "the validation passes but the connector returns a bad request status" in {
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(BAD_REQUEST, errorResponse)
        requestNinoCallIsSuccessful(Left(errorResponseWithStatus))

        val requestWithToken = request.withHeaders(AUTHORIZATION -> "token")

        await(controller.postByService("service-a")(requestWithToken)) mustEqual withHeaders(
          BadRequest(Json.toJson(errorResponse))
        )
      }

    }

  }

}
