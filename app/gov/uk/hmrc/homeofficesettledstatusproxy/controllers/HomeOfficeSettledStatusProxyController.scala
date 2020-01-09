package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import java.util.UUID

import gov.uk.hmrc.homeofficesettledstatusproxy.connectors.HomeOfficeRightToPublicFundsConnector
import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckError, StatusCheckResponse, StatusCheckResult, ValidationError}
import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.{JsError, JsObject, JsSuccess, JsValue, Json}
import play.api.mvc._
import play.api.{Configuration, Environment}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusProxyController @Inject()(
  rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
  val env: Environment,
  cc: ControllerComponents)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc) {

  val HEADER_X_CORRELATION_ID = "x-correlation-id"

  def statusPublicFundsByNino: Action[JsValue] = Action.async(parse.tolerantJson) {
    implicit request =>
      val correlationId =
        request.headers.get("x-correlation-id").getOrElse(UUID.randomUUID().toString)

      request.body.asOpt[JsObject] match {

        case None =>
          val result = StatusCheckResponse.error(correlationId, "ERR_REQUEST_INVALID")
          Future.successful(BadRequest(Json.toJson(result)))

        case Some(payload) =>
          validate(payload) match {

            case ValidRequest(statusCheckByNinoRequest) =>
              rightToPublicFundsConnector
                .statusPublicFundsByNino(statusCheckByNinoRequest, correlationId)
                .map {
                  case result @ StatusCheckResponse(_, None, Some(_)) => Ok(Json.toJson(result))

                  case result @ StatusCheckResponse(
                        _,
                        Some(StatusCheckError(Some(errCode), _)),
                        None) =>
                    errCode match {
                      case "ERR_NOT_FOUND"  => NotFound(Json.toJson(result))
                      case "ERR_VALIDATION" => UnprocessableEntity(Json.toJson(result))
                      case _                => BadRequest(Json.toJson(result)) // e.g. ERR_REQUEST_INVALID
                    }
                }
                .map(_.withHeaders(
                  HEADER_X_CORRELATION_ID  -> correlationId,
                  HeaderNames.CONTENT_TYPE -> MimeTypes.JSON))

            case MissingInputFields(fields) =>
              val result =
                StatusCheckResponse
                  .error(correlationId, "ERR_REQUEST_INVALID", Some(fields.map((_, "missing"))))
              Future.successful(BadRequest(Json.toJson(result)))

            case InvalidInputFields(validationErrors) =>
              val result =
                StatusCheckResponse.error(correlationId, "ERR_VALIDATION", Some(validationErrors))
              Future.successful(UnprocessableEntity(Json.toJson(result)))
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

sealed trait ValidationResult

case class ValidRequest(request: StatusCheckByNinoRequest) extends ValidationResult
case class MissingInputFields(fields: List[String]) extends ValidationResult
case class InvalidInputFields(fields: List[(String, String)]) extends ValidationResult
