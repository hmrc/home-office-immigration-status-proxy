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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import models.{OAuthToken, StatusCheckByNinoRequest, StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse, StatusCheckResult}
import play.api.mvc.Results._
import play.api.http.Status._
import play.api.mvc.Result
import java.time.LocalDate
import uk.gov.hmrc.domain.Nino
import play.mvc.Http.HeaderNames
import play.api.http.MimeTypes
import connectors.ErrorCodes._

class NinoSearchControllerSpec extends ControllerSpec {

  val controller = inject[NinoSearchController]
  val correlationId = "CorrelationId123"

  "post" should {

    val requestBody =
      Json.toJson(StatusCheckByNinoRequest("2001-01-31", "Jane", "Doe", Nino("RJ301829A")))
    val request: FakeRequest[JsValue] =
      FakeRequest().withBody(requestBody).withHeaders("X-Correlation-Id" -> correlationId)

    "fail" when {

      "connector.token fails" in {
        tokenCallFails
        intercept[Exception](await(controller.post(request)))
      }

      "connector.token is successful but connector.statusPublicFundsByNino fails" in {
        tokenCallIsSuccessful
        requestCallFails
        intercept[Exception](await(controller.post(request)))
      }

    }

    "return 200" when {

      "the connector calls are successful and the validation passes" in {
        tokenCallIsSuccessful
        val statusCheckResult = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
        val statusCheckResponse = StatusCheckResponse("CorrelationId", statusCheckResult)
        requestCallIsSuccessful(Right(statusCheckResponse))
        await(controller.post(request)) mustEqual withHeaders(Ok(Json.toJson(statusCheckResponse)))
      }

    }

    "return an error" when {

      "the validation passes but the connector returns an internal error status" in {
        tokenCallIsSuccessful
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(INTERNAL_SERVER_ERROR, errorResponse)
        requestCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(
          InternalServerError(Json.toJson(errorResponse)))
      }

      "the validation passes but the connector returns a bad request status" in {
        tokenCallIsSuccessful
        val errorResponse =
          StatusCheckErrorResponse(None, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
        val errorResponseWithStatus =
          StatusCheckErrorResponseWithStatus(BAD_REQUEST, errorResponse)
        requestCallIsSuccessful(Left(errorResponseWithStatus))
        await(controller.post(request)) mustEqual withHeaders(
          BadRequest(Json.toJson(errorResponse)))
      }

    }

  }

  def tokenCallFails =
    when(mockConnector.token(any())(any()))
      .thenReturn(Future.failed(new Exception("Oh no - token")))
  def tokenCallIsSuccessful =
    when(mockConnector.token(any())(any()))
      .thenReturn(Future.successful(OAuthToken("String", "String")))
  def requestCallFails =
    when(mockConnector.statusPublicFundsByNino(any(), any(), any())(any()))
      .thenReturn(Future.failed(new Exception("Oh no - connector")))
  def requestCallIsSuccessful(
    response: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]) =
    when(mockConnector.statusPublicFundsByNino(any(), any(), any())(any()))
      .thenReturn(Future.successful(response))

  def withHeaders(result: Result): Result =
    result
      .withHeaders("X-Correlation-Id" -> correlationId, HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)

}
