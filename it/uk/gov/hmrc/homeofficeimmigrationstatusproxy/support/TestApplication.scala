/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support

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
