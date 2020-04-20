package uk.gov.hmrc.homeofficesettledstatusproxy.controllers

import play.api.Application
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.homeofficesettledstatusproxy.support.AppBaseISpec
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class AuthActionsISpec extends AuthActionISpecSetup {

  "withAuthorisedWithStrideGroup" should {

    "call body when authorised" in {

      givenAuthorisedForStride

      val result = TestController.withAuthorisedWithStride

      bodyOf(result) should include("foo")
      status(result) shouldBe 200

    }

    "return 403 Forbidden otherwise" in {
      givenRequestIsNotAuthorised("")

      val result = TestController.withAuthorisedWithStride
      status(result) shouldBe 403
    }

  }

}

trait AuthActionISpecSetup extends AppBaseISpec {

  override def fakeApplication: Application = appBuilder.build()

  object TestController extends AuthActions {

    override def authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")

    import scala.concurrent.ExecutionContext.Implicits.global

    def withAuthorisedWithStride[A]: Result =
      await(super.authorisedWithStride {
        Future.successful(Ok("foo"))
      })

  }

}
