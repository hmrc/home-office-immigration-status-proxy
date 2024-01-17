/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import stubs.{AuthStubs, DataStreamStubs}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
abstract class BaseISpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with WireMockSupport
    with AuthStubs
    with DataStreamStubs {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  override def commonStubs(): Unit =
    givenAuditConnector()

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(request.withHeaders(request.headers), request.session)

}