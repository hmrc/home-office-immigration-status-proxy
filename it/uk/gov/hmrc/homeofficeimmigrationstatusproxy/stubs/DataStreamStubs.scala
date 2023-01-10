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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import support.WireMockSupport
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}

trait DataStreamStubs extends Eventually {
  me: WireMockSupport =>

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5, Seconds)), scaled(Span(500, Millis)))

  def givenAuditConnector(): Unit = {
    stubFor(post(urlPathEqualTo(auditUrl)).willReturn(aResponse().withStatus(204)))
    stubFor(post(urlPathEqualTo(auditUrl + "/merged")).willReturn(aResponse().withStatus(204)))
  }

  private def auditUrl = "/write/audit"

}
