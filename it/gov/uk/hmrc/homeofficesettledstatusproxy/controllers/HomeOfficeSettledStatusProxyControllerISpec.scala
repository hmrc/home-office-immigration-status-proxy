package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.Json
import play.api.libs.ws.{ WSClient, WSResponse }
import gov.uk.hmrc.homeofficesettledstatusproxy.support.ServerBaseISpec

class HomeOfficeSettledStatusProxyControllerISpec extends ServerBaseISpec {

  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port/home-office-settled-status-proxy"

  val wsClient = app.injector.instanceOf[WSClient]

  def entity(): WSResponse = {
    wsClient.url(s"$url/entities")
      .get()
      .futureValue
  }

  "HomeOfficeSettledStatusProxyController" when {

    "GET /entities" should {
      "respond with some data" in {
        val result = entity()
        result.status shouldBe 200
        result.json shouldBe Json.obj("parameter1" -> "hello world")
      }
    }
  }
}
