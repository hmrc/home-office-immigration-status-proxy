package uk.gov.hmrc.homeofficesettledstatusproxy.support

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait TestApplication {
  _: BaseISpec =>

  override implicit lazy val app: Application = appBuilder.build()

  protected override def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.home-office-right-to-public-funds.port"       -> wireMockPort,
        "microservice.services.home-office-right-to-public-funds.host"       -> wireMockHost,
        "microservice.services.home-office-right-to-public-funds.pathPrefix" -> "/v1",
        "microservice.services.auth.port"                                    -> wireMockPort,
        "microservice.services.auth.host"                                    -> wireMockHost,
        "metrics.enabled"                                                    -> true,
        "auditing.enabled"                                                   -> true,
        "auditing.consumer.baseUri.host"                                     -> wireMockHost,
        "auditing.consumer.baseUri.port"                                     -> wireMockPort,
        "play.http.router"                                                   -> "testOnlyDoNotUseInAppConf.Routes"
      )

}
