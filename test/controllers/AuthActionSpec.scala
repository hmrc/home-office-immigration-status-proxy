/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent, BodyParsers}
import play.api.test.Helpers.{FORBIDDEN, OK, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.auth.core.{AuthConnector, MissingBearerToken}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  private class Harness(authAction: AuthAction) {
    def onPageLoad(): Action[AnyContent] = authAction { implicit request =>
      Ok(Json.obj())
    }
  }

  protected def fakeRequest = FakeRequest("", "")

  val bodyParsers: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val authAction = new AuthActionImpl(
    mockAuthConnector,
    bodyParsers
  )(global)

  "auth action" must {
    "return OK when authorised" in {
      val controller = new Harness(authAction)
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe OK
    }
    "return FORBIDDEN when AuthorisationException (missing bearer token)" in {
      val controller = new Harness(authAction)

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.failed(MissingBearerToken()))

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe FORBIDDEN
    }
    "not catch ordinary exceptions" in {
      val controller = new Harness(authAction)

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenThrow(new RuntimeException("error"))

      intercept[RuntimeException] {
        controller.onPageLoad()(fakeRequest)
      }
    }

  }

}
