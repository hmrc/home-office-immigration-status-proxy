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

import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.mvc._
import play.api.{Configuration, Environment}
import play.mvc.Http.HeaderNames
import connectors.HomeOfficeRightToPublicFundsConnector
import models.StatusCheckByMrzRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import wiring.Constants._

import scala.concurrent.ExecutionContext

@Singleton
class MrzSearchController @Inject()(
  rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
  authAction: AuthAction,
  val env: Environment,
  cc: ControllerComponents)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc) with BaseController {

  def post: Action[JsValue] = authAction.async(parse.tolerantJson) { implicit request =>
    val correlationId = getCorrelationId

    withValidParameters[StatusCheckByMrzRequest](correlationId) { statusCheckByMrzRequest =>
      for {
        token <- rightToPublicFundsConnector.token(correlationId)
        either <- rightToPublicFundsConnector
                   .statusPublicFundsByMrz(statusCheckByMrzRequest, correlationId, token)
        result = eitherToResult(either)
      } yield
        result.withHeaders(
          HEADER_X_CORRELATION_ID  -> correlationId,
          HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
    }

  }

}
