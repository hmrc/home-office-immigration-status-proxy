package controllers

import java.util.UUID

import org.scalatest.Suite
import org.scalatestplus.play.ServerProvider
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}
import stubs.HomeOfficeRightToPublicFundsStubs
import support.{JsonMatchers, ServerBaseISpec}
import connectors.ErrorCodes._

class NinoSearchControllerISpec
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

  def publicFundsByNinoRaw(payload: String, correlationId: String = "sjdfhks123"): WSResponse =
    wsClient
      .url(s"$url/v1/status/public-funds/nino/raw")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addHttpHeaders((if (correlationId.isEmpty) "" else "x-correlation-id") -> correlationId)
      .post(payload)
      .futureValue

  "NinoSearchController" when {

    "POST /v1/status/public-funds/nino" should {

      "respond with 200 if request is valid" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckResultNoRangeExample()
        givenAuthorisedForStride

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
        givenAuthorisedForStride

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 404
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_NOT_FOUND))
          ))
      }

      "respond with 400 if one of the required input parameters is missing from the request" in {
        ping.status.shouldBe(200)

        givenAuthorisedForStride

        val correlationId = UUID.randomUUID().toString
        val result = publicFundsByNino("{}", correlationId)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be(correlationId))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed internal validation" in {
        ping.status.shouldBe(200)

        givenAuthorisedForStride

        val result = publicFundsByNino(invalidNinoRequestBody)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_VALIDATION))
              and havePropertyArrayOf[JsObject](
                "fields",
                haveProperty[String]("code")
                  and haveProperty[String]("name")
              )
          ))
      }

      "respond with 400 if one of the input parameters passed in has failed external validation" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorWhenDOBInvalid()
        givenAuthorisedForStride

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_VALIDATION))
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
        givenAuthorisedForStride

        val result = publicFundsByNino("[]")

        result.status shouldBe 400
        result.json.as[JsObject] should (haveProperty[String]("correlationId", be("sjdfhks123"))
          and haveProperty[JsObject](
            "error",
            haveProperty[String]("errCode", be(ERR_REQUEST_INVALID))))
      }

      "respond with 400 even if the service error undefined" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(400)
        givenAuthorisedForStride

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 400
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json.as[JsObject] should haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 404 even if the service error undefined" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(404)
        givenAuthorisedForStride

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 404
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json.as[JsObject] should haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }

      "respond with 409 even if the service error undefined" in {
        ping.status.shouldBe(200)

        givenOAuthTokenGranted()
        givenStatusCheckErrorUndefined(409)
        givenAuthorisedForStride

        val result = publicFundsByNino(validRequestBody)

        result.status shouldBe 409
        result.json.as[JsObject] should haveProperty[String]("correlationId")
        result.json.as[JsObject] should haveProperty[JsObject](
          "error",
          haveProperty[String]("errCode", be(ERR_UNKNOWN)))
      }
    }
  }
}
