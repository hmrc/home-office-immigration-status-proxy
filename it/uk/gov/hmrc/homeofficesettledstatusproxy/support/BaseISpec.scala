package uk.gov.hmrc.homeofficesettledstatusproxy.support

import akka.stream.Materializer
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.homeofficesettledstatusproxy.stubs.{AuthStubs, DataStreamStubs}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.Future
abstract class BaseISpec
    extends AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with WireMockSupport with AuthStubs with DataStreamStubs with MetricsTestSupport {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  override def commonStubs(): Unit = {
    givenCleanMetricRegistry()
    givenAuditConnector()
  }

  protected implicit def materializer: Materializer = app.materializer

  protected def checkHtmlResultWithBodyText(result: Future[Result], expectedSubstring: String): Unit = {
    status(result) shouldBe 200
    contentType(result) shouldBe Some("text/html")
    charset(result) shouldBe Some("utf-8")
    contentAsString(result) should include(expectedSubstring)
  }

  private lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  private implicit def messages: Messages = messagesApi.preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(request.withHeaders(request.headers), request.session)

}
