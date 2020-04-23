/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatusproxy.controllers.testonly

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.homeofficesettledstatusproxy.connectors.{HomeOfficeRightToPublicFundsConnector, MicroserviceAuthConnector}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class HomeOfficeSettledStatusProxyTestOnlyController @Inject()(
  rightToPublicFundsConnector: HomeOfficeRightToPublicFundsConnector,
  val env: Environment,
  cc: ControllerComponents)(implicit val configuration: Configuration, ec: ExecutionContext)
    extends BackendController(cc) {

  val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

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

}
