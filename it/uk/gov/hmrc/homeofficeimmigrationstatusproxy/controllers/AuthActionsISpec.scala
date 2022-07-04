package controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.mvc.Results._
import play.api.test.FakeRequest
import support.AppBaseISpec
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import play.api.test.Helpers.{contentAsString, status, _}

import scala.concurrent.Future

class AuthActionsISpec extends AuthActionISpecSetup with ScalaFutures {

  "withAuthorisedWithStrideGroup" should {

    "call body when authorised" in {

      givenAuthorisedForStride

      val result: Future[Result] = TestController.withAuthorisedWithStride

      contentAsString(result) should include("foo")
      status(result) shouldBe 200

    }

    "return 403 Forbidden when AuthorisationException" in {
      givenRequestIsNotAuthorised("")

      val result = TestController.withAuthorisedWithStride
      status(result) shouldBe 403
    }

    "do not catch ordinary exceptions" in {
      givenAuthorisedForStride

      intercept[RuntimeException]{
        val result: Future[Result] = TestController.withAuthorisedWithStride {
          throw new RuntimeException
        }
        result.futureValue
      }
    }

  }

}

trait AuthActionISpecSetup extends AppBaseISpec {

  override def fakeApplication: Application = appBuilder.build()

  object TestController {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest()
        .withHeaders(AUTHORIZATION -> "Bearer 123")
        .withSession(SessionKeys.authToken -> "Bearer XYZ")

    val sut = app.injector.instanceOf[AuthAction]

    def withAuthorisedWithStride: Future[Result] = sut(Ok("foo"))(request)

    def withAuthorisedWithStride(raiseException: => Result): Future[Result] = sut(raiseException)(request)

  }

}
