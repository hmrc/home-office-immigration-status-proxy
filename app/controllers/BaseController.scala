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

import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import models.{StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import connectors.ErrorCodes._
import java.util.UUID

import scala.concurrent.Future

trait BaseController {

  val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  def mandatoryFields: Set[String]

  def withValidParameters[A](correlationId: String)(
    f: A => Future[Result])(implicit request: Request[JsValue], reads: Reads[A]): Future[Result] =
    request.body.asOpt[JsObject] match {
      case None =>
        val result = StatusCheckErrorResponse.error(Some(correlationId), ERR_REQUEST_INVALID)
        Future.successful(BadRequest(Json.toJson(result)))
      case Some(json) =>
        validate(json, f, correlationId)
    }

  def validate[A](json: JsObject, f: A => Future[Result], correlationId: String)(
    implicit reads: Reads[A]): Future[Result] =
    validateJson(json) match {
      case ValidRequest(r) =>
        f(r)
      case MissingInputFields(fields) =>
        val result =
          StatusCheckErrorResponse
            .error(
              Some(correlationId),
              ERR_REQUEST_INVALID,
              Some(fields.map(f => (s"/$f", "missing"))))
        Future.successful(BadRequest(Json.toJson(result)))
      case InvalidInputFields(validationErrors) =>
        val result =
          StatusCheckErrorResponse
            .error(Some(correlationId), ERR_VALIDATION, Some(validationErrors))
        Future.successful(BadRequest(Json.toJson(result)))
    }

  def validateJson[A](json: JsObject)(implicit reads: Reads[A]): ValidationResult[A] = {
    val providedFields: Set[String] = json.fields.map(_._1).toSet
    val missingFields = mandatoryFields.diff(providedFields)
    if (missingFields.nonEmpty) MissingInputFields(missingFields.toList)
    else
      json.validate[A] match {
        case JsSuccess(result, _) => ValidRequest(result)
        case JsError(errors) =>
          val validationErrors =
            errors.flatMap { case (p, ve) => ve.map(e => (p.toString, e.message)) }.toList
          InvalidInputFields(validationErrors)
      }
  }

  def eitherToResult(
    response: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]): Result =
    response.fold(
      e => new Status(e.statusCode)(Json.toJson(e.errorResponse)),
      r => Ok(Json.toJson(r))
    )

  def getCorrelationId(implicit request: Request[JsValue]) =
    request.headers.get(HEADER_X_CORRELATION_ID).getOrElse(UUID.randomUUID().toString)

}
