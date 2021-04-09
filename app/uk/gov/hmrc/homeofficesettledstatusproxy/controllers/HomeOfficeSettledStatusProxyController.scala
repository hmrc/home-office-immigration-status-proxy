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

package uk.gov.hmrc.homeofficesettledstatusproxy.controllers

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.mvc._
import play.api.{Configuration, Environment}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.homeofficesettledstatusproxy.connectors.{HomeOfficeRightToPublicFundsConnector, MicroserviceAuthConnector}
import uk.gov.hmrc.homeofficesettledstatusproxy.models.StatusCheckResponse.{HasError, HasResult}
import uk.gov.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckResponse}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.homeofficesettledstatusproxy.connectors.ErrorCodes._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusProxyController @Inject()(
  rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
  val authConnector: MicroserviceAuthConnector,
  val env: Environment,
  cc: ControllerComponents)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc) with AuthActions {

  val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  def statusPublicFundsByNino: Action[JsValue] = Action.async(parse.tolerantJson) {
    implicit request =>
      authorisedWithStride {

        val correlationId =
          request.headers.get(HEADER_X_CORRELATION_ID).getOrElse(UUID.randomUUID().toString)

        request.body.asOpt[JsObject] match {

          case None =>
            val result = StatusCheckResponse.error(correlationId, ERR_REQUEST_INVALID)
            Future.successful(BadRequest(Json.toJson(result)))

          case Some(payload) =>
            validate(payload) match {

              case ValidRequest(statusCheckByNinoRequest) =>
                rightToPublicFundsConnector
                  .token(correlationId)
                  .flatMap { token =>
                    rightToPublicFundsConnector
                      .statusPublicFundsByNino(statusCheckByNinoRequest, correlationId, token)
                      .map {
                        case HasError(ERR_NOT_FOUND, response) =>
                          NotFound(Json.toJson(response))

                        case HasError(ERR_VALIDATION, response) =>
                          BadRequest(Json.toJson(response))

                        case HasError(ERR_CONFLICT, response) =>
                          Conflict(Json.toJson(response))

                        case HasResult(response) => Ok(Json.toJson(response))

                        case response => BadRequest(Json.toJson(response))
                      }
                  }
                  .map(_.withHeaders(
                    HEADER_X_CORRELATION_ID  -> correlationId,
                    HeaderNames.CONTENT_TYPE -> MimeTypes.JSON))

              case MissingInputFields(fields) =>
                val result =
                  StatusCheckResponse
                    .error(correlationId, ERR_REQUEST_INVALID, Some(fields.map((_, "missing"))))
                Future.successful(BadRequest(Json.toJson(result)))

              case InvalidInputFields(validationErrors) =>
                val result =
                  StatusCheckResponse.error(correlationId, ERR_VALIDATION, Some(validationErrors))
                Future.successful(BadRequest(Json.toJson(result)))
            }
        }
      }
  }

  def statusPublicFundsByNinoRaw: Action[RawBuffer] = Action.async(parse.raw) { implicit request =>
    val correlationId =
      request.headers.get(HEADER_X_CORRELATION_ID).getOrElse(UUID.randomUUID().toString)

    rightToPublicFundsConnector
      .token(correlationId)
      .flatMap { token =>
        rightToPublicFundsConnector
          .statusPublicFundsByNinoRaw(
            request.body
              .asBytes()
              .map(_.utf8String)
              .getOrElse(throw new IllegalArgumentException(
                "Failed to read request's body as an utf-8 string.")),
            correlationId,
            token
          )
          .map { response =>
            new Status(response.status)(response.body)
              .withHeaders(response.allHeaders.toSeq.flatMap {
                case (key, values) => values.map(v => (key, v))
              }: _*)
          }
      }
  }

  def validate(json: JsObject): ValidationResult = {
    val providedFields: Set[String] = json.fields.map(_._1).toSet
    val missingFields = StatusCheckByNinoRequest.mandatoryFields.diff(providedFields)
    if (missingFields.nonEmpty) MissingInputFields(missingFields.toList)
    else
      json.validate[StatusCheckByNinoRequest] match {
        case JsSuccess(result, _) => ValidRequest(result)

        case JsError(errors) =>
          val validationErrors =
            errors.flatMap { case (p, ve) => ve.map(e => (p.toString, e.message)) }.toList
          InvalidInputFields(validationErrors)
      }
  }

}
