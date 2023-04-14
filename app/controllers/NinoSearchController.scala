/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.HomeOfficeRightToPublicFundsConnector
import controllers.BaseController
import models.StatusCheckByNinoRequest
import play.api.Configuration
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.mvc._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.internalauth.client.{BackendAuthComponents, IAAction, Predicate, Resource}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import wiring.Constants._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoSearchController @Inject() (
                                       rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
                                       authAction: AuthAction,
                                       internalAuthAction: BackendAuthComponents,
                                       cc: ControllerComponents
)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc)
    with BaseController {

  private def processRequest(implicit request: Request[JsValue]): Future[Result] = {
    val correlationId = getCorrelationId

    withValidParameters[StatusCheckByNinoRequest](correlationId) { statusCheckByNinoRequest =>
      for {
        token <- rightToPublicFundsConnector.token(correlationId, hc.requestId)
        either <- rightToPublicFundsConnector
          .statusPublicFundsByNino(statusCheckByNinoRequest, correlationId, hc.requestId, token)
        result = eitherToResult(either)
      } yield result.withHeaders(HEADER_X_CORRELATION_ID -> correlationId, HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
    }
  }

  def post: Action[JsValue] = authAction.async(parse.tolerantJson){
    implicit request =>
      processRequest
  }

  private def permission(service: String) = Predicate.Permission(
    resource = Resource.from(
      "home-office-immigration-status-proxy",
      s"status/public-funds/nino/$service"
    ),
    action = IAAction("WRITE")
  )

  def postByService(service: String): Action[JsValue] = internalAuthAction.authorizedAction(permission(service)).async(parse.tolerantJson) {
    implicit request: Request[JsValue] =>
      processRequest
  }

}
