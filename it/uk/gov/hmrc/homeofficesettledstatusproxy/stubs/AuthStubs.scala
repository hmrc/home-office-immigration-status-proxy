package uk.gov.hmrc.homeofficesettledstatusproxy.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.homeofficesettledstatusproxy.support.WireMockSupport

trait AuthStubs {
  me: WireMockSupport =>

  def givenRequestIsNotAuthorised(mdtpDetail: String): AuthStubs = {
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$mdtpDetail"""")))
    this
  }

  def givenAuthorisedForStride: AuthStubs = {
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(1)
        .withRequestBody(equalToJson(
          s"""
             |{
             |  "authorise": [
             |    {
             |      "authProviders": [
             |        "PrivilegedApplication"
             |      ]
             |    }
             |  ]
             |}
           """.stripMargin,
          true,
          true
        ))
        .willReturn(aResponse()
          .withBody("{}")
          .withStatus(200)))

    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .atPriority(2)
        .willReturn(aResponse()
          .withStatus(401)
          .withHeader("WWW-Authenticate", "MDTP detail=\"InsufficientEnrolments\"")))
    this
  }

  def verifyAuthoriseAttempt(): Unit =
    verify(1, postRequestedFor(urlEqualTo("/auth/authorise")))

}
