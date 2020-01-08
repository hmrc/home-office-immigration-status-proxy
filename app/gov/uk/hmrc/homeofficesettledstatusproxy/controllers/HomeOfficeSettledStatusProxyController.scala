package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import gov.uk.hmrc.homeofficesettledstatusproxy.connectors.HomeOfficeRightToPublicFundsConnector
import gov.uk.hmrc.homeofficesettledstatusproxy.models.{StatusCheckByNinoRequest, StatusCheckError, StatusCheckResponse, StatusCheckResult, ValidationError}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusProxyController @Inject()(
  rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
  val env: Environment,
  cc: ControllerComponents)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc) {

  val HEADER_CORRELATION_ID = "x-correlation-id"

  def statusPublicFundsByNino: Action[JsValue] = Action.async(parse.tolerantJson) { implicit request =>
    request.body.validate[StatusCheckByNinoRequest] match {

      case JsSuccess(statusCheckByNinoRequest, _) =>
        rightToPublicFundsConnector
          .statusPublicFundsByNino(statusCheckByNinoRequest)
          .map(result => Ok(Json.toJson(result)))

      case JsError(errors) =>
        val correlationId = request.headers.get(HEADER_CORRELATION_ID).getOrElse("unknown")
        val validationErrors =
          errors.flatMap { case (p, ve) => ve.map(e => ValidationError(e.message, p.toString)) }.toList
        val result = StatusCheckResponse(
          error = Some(StatusCheckError(fields = Some(validationErrors))),
          correlationId = correlationId)
        Future.successful(Ok(Json.toJson(result)))
    }
  }

}
