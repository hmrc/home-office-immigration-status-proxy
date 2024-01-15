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

import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import models.{StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import connectors.ErrorCodes._

import java.util.UUID
import wiring.Constants._

import scala.concurrent.Future

trait BaseController {

  def withValidParameters[A](
    correlationId: String
  )(f: A => Future[Result])(implicit request: Request[JsValue], reads: Reads[A]): Future[Result] =
    request.body.validate[A] match {
      case JsSuccess(result, _) => f(result)
      case JsError(errors)      => jsErrorToResponse(correlationId, errors.toSeq)
    }

  private def jsErrorToResponse(
    correlationId: String,
    errors: Seq[(JsPath, scala.collection.Seq[JsonValidationError])]
  ): Future[Result] = {
    val validationErrors =
      errors.flatMap { case (path, err) => err.map(e => (path.toString, e.message)) }.toList
    val result =
      StatusCheckErrorResponse
        .error(Some(correlationId), ERR_REQUEST_INVALID, Some(validationErrors))
    Future.successful(BadRequest(Json.toJson(result)))
  }

  def eitherToResult(response: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]): Result =
    response.fold(
      e => new Status(e.statusCode)(Json.toJson(e.errorResponse)),
      r => Ok(Json.toJson(r))
    )

  def getCorrelationId(implicit request: Request[JsValue]): String =
    request.headers.get(HEADER_X_CORRELATION_ID).getOrElse(UUID.randomUUID().toString)

}
