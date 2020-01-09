package gov.uk.hmrc.homeofficesettledstatusproxy.controllers

import gov.uk.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import gov.uk.hmrc.homeofficesettledstatusproxy.support.{JsonMatchers, ServerBaseISpec}
import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}

class HomeOfficeSettledStatusProxyControllerISpec
    extends ServerBaseISpec with HomeOfficeRightToPublicFundsStubs with JsonMatchers {
  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port"

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def ping: WSResponse = wsClient.url(s"$url/ping/ping").get.futureValue

  def publicFundsByNino(payload: String): WSResponse =
    wsClient
      .url(s"$url/status/public-funds/nino")
      .addHttpHeaders("Content-Type" -> "application/json")
      .post(payload)
      .futureValue

  "HomeOfficeSettledStatusProxyController" when {

    "POST /status/public-funds/nino" should {
      "respond with status if request is valid" in {
        ping.status.shouldBe(200)

        givenStatusCheckResultNoRangeExample()

        val result = publicFundsByNino(requestBodyNoRange)
        result.status shouldBe 200
        result.json.as[JsObject] should (haveProperty[String]("correlationId")
          and haveProperty[JsObject](
            "result",
            haveProperty[String]("dateOfBirth", be("2001-01-31"))
              and haveProperty[String]("facialImage", be("string"))
              and haveProperty[String]("fullName", be("Jane Doe"))
              and havePropertyArrayOf[JsObject](
                "statuses",
                haveProperty[String]("immigrationStatus", be("ILR"))
                  and haveProperty[Boolean]("rightToPublicFunds", be(true))
                  and haveProperty[String]("statusEndDate", be("2018-01-31"))
                  and haveProperty[String]("statusStartDate", be("2018-12-12"))
              )
          ))
      }

      "respond with error if request is not valid" in {
        ping.status.shouldBe(200)

        val result = publicFundsByNino("{}")

        result.status shouldBe 200
        result.json.as[JsObject] should (haveProperty[String]("correlationId")
          and haveProperty[JsObject](
            "error",
            havePropertyArrayOf[JsObject](
              "fields",
              haveProperty[String]("code")
                and haveProperty[String]("name")
            )
          ))
      }
    }
  }
}
