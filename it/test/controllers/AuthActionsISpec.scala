/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.mvc.Results.*
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status, *}
import support.{AppBaseISpec, BaseISpec}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class AuthActionsISpec extends AuthActionISpecSetup {

  "withAuthorisedWithStrideGroup" should {

    "call body when authorised" in {

      givenAuthorisedForStride

      val result: Future[Result] = TestController.withAuthorisedWithStride

      contentAsString(result) should include("foo")
      status(result)        shouldBe 200

    }

    "return 403 Forbidden when AuthorisationException" in {
      givenRequestIsNotAuthorised("")

      val result = TestController.withAuthorisedWithStride
      status(result) shouldBe 403
    }

    "do not catch ordinary exceptions" in {
      givenAuthorisedForStride

      intercept[RuntimeException] {
        val result: Future[Result] = TestController.withAuthorisedWithStride {
          throw new RuntimeException
        }
        result.futureValue
      }
    }

  }

}

trait AuthActionISpecSetup extends AppBaseISpec {

  override def fakeApplication(): Application = appBuilder.build()

  object TestController {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest()
        .withHeaders(AUTHORIZATION -> "Bearer 123")
        .withSession(SessionKeys.authToken -> "Bearer XYZ")

    val sut: AuthAction = app.injector.instanceOf[AuthAction]

    def withAuthorisedWithStride: Future[Result] = sut(Ok("foo"))(request)

    def withAuthorisedWithStride(raiseException: => Result): Future[Result] = sut(raiseException)(request)

  }

}
