package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusProxyController @Inject()(val env: Environment, cc: ControllerComponents)(
  implicit val configuration: Configuration,
  ec: ExecutionContext)
    extends BackendController(cc) {

  def publicFundsByNino: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(Json.obj()))
  }

}
