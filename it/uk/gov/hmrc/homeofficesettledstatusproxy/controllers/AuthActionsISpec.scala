package uk.gov.hmrc.homeofficesettledstatusproxy.controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.mvc.Results._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.homeofficesettledstatusproxy.support.AppBaseISpec
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
      play.api.test.Helpers.status(result) shouldBe 403
    }

    "do not catch ordinary exceptions" in {
      givenAuthorisedForStride

      intercept[RuntimeException]{
        val result: Future[Result] =TestController.withAuthorisedWithStride {
          throw new RuntimeException
        }
        result.futureValue
      }
    }

  }

}

trait AuthActionISpecSetup extends AppBaseISpec {

  override def fakeApplication: Application = appBuilder.build()

  object TestController extends AuthActions {

    override def authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")

    import scala.concurrent.ExecutionContext.Implicits.global

    def withAuthorisedWithStride: Future[Result] =
        super.authorisedWithStride {
        Future.successful(Ok("foo"))
      }

    def withAuthorisedWithStride(raiseException: => Nothing): Future[Result] =
        super.authorisedWithStride {
        Future.successful(raiseException)
      }

  }

}
