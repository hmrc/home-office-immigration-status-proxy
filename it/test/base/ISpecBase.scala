/*
 * Copyright 2026 HM Revenue & Customs
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

package base

import common.TestData
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import support.{JsonMatchers, WireMockSupport}

import scala.concurrent.Future

trait ISpecBase
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with WireMockSupport
    with JsonMatchers
    with ScalaFutures
    with IntegrationPatience
    with WireMockStubs
    with TestData {

  protected lazy val app: Application = appBuilder.build()

  private def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.home-office-right-to-public-funds.port" -> wireMockServer.port(),
        "microservice.services.internal-auth.port"                     -> wireMockServer.port(),
        "microservice.services.auth.port"                              -> wireMockServer.port(),
        "metrics.enabled"                                              -> false,
        "auditing.enabled"                                             -> false,
        "auditing.consumer.baseUri.port"                               -> wireMockServer.port()
      )

  protected def post(url: String, payload: String, correlationId: String = correlationId): Future[Result] = {
    val hdrs: Seq[(String, String)] = Seq(
      AUTHORIZATION      -> bearerId,
      "x-correlation-id" -> correlationId
    )

    val request = FakeRequest(POST, url)
      .withHeaders(hdrs*)
      .withJsonBody(Json.parse(payload))
    route(app, request).get
  }
}
