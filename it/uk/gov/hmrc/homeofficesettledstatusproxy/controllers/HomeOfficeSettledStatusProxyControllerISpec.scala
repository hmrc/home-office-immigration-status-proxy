package uk.gov.hmrc.homeofficesettledstatusproxy.controllers

import java.util.UUID

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.homeofficesettledstatusproxy.stubs.HomeOfficeRightToPublicFundsStubs
import uk.gov.hmrc.homeofficesettledstatusproxy.support.{JsonMatchers, ServerBaseISpec}

class HomeOfficeSettledStatusProxyControllerISpec
    extends ServerBaseISpec with HomeOfficeRightToPublicFundsStubs with JsonMatchers {
  this: Suite with ServerProvider =>

  val url = s"http://localhost:$port"

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  def ping: WSResponse = wsClient.url(s"$url/ping/ping").get.futureValue

  def publicFundsByNino(payload: String, correlationId: String = "sjdfhks123"): WSResponse =
    wsClient
      .url(s"$url/v1/status/public-funds/nino")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addHttpHeaders((if (correlationId.isEmpty) "" else "x-correlation-id") -> correlationId)
      .post(payload)
      .futureValue

  "HomeOfficeSettledStatusProxyController" when {

    "POST /v1/status/public-funds/nino" should {

      "respond with 200 if request is valid" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample()

        val result = publicFundsByNino(validRequestBody)
        result.status shouldBe 200
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "result",
            haveProperty[String]("dateOfBirth", be("2001-01-31"))
              and haveProperty[String]("nationality", be("IRL"))
              and haveProperty[String]("fullName", be("Jane Doe"))
              and havePropertyArrayOf[JsObject](
                "statuses",
                haveProperty[String]("productType", be("EUS"))
                  and haveProperty[String]("immigrationStatus", be("ILR"))
                  and haveProperty[Boolean]("noRecourseToPublicFunds", be(true))
                  and haveProperty[String]("statusEndDate", be("2018-01-31"))
                  and haveProperty[String]("statusStartDate", be("2018-12-12"))
              )
          ))
      }

      "respond with 404 if the service failed to find an identity based on the values provided" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenStatusNotFound()

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 404
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be("ERR_NOT_FOUND"))
          ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        ping.status.shouldBe(200)

        val correlationId = UUID.randomUUID().toString
        val result = publicFundsByNino("{}", correlationId)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be("ERR_REQUEST_INVALID"))
          ))
      }

      "respond with 422 if one of the input parameters passed in has failed internal validation" in {
        ping.status.shouldBe(200)

        val result = publicFundsByNino(invalidNinoRequestBody)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be("ERR_VALIDATION"))
              and havePropertyArrayOf[JsObject](
                "fields",
                haveProperty[String]("code")
                  and haveProperty[String]("name")
              )
          ))
      }

      "respond with 422 if one of the input parameters passed in has failed external validation" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid()

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be("ERR_VALIDATION"))
              and havePropertyArrayOf[JsObject](
                "fields",
                haveProperty[String]("code")
                  and haveProperty[String]("name")
              )
          ))
      }

      "respond with 400 if request payload is invalid json" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()

        val result = publicFundsByNino("[]")

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be("ERR_REQUEST_INVALID"))))
      }

      "respond with 400 if the service response undefined" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckResponseUndefined()

        val result = publicFundsByNino(validRequestBody, "")

        result.status shouldBe 400
        result.json.as[JsObject] should haveProperty[String]("correlationId")
      }
    }
  }
}
