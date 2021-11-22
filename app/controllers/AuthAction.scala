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

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import play.api.Environment
import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(
  val env: Environment,
  override val authConnector: AuthConnector,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](
    request: Request[A],
    block: Request[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(PrivilegedApplication))(block(request))
      .recover {
        case e: AuthorisationException =>
          Forbidden("PrivilegedApplication required.")
      }
  }

}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[Request, AnyContent]
