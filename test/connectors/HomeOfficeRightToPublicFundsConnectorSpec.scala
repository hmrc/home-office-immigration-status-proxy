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

package connectors

import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.http.RequestId
import uk.gov.hmrc.http.client.HttpClientV2
import wiring.AppConfig

import scala.concurrent.ExecutionContext.global
import scala.language.postfixOps

class HomeOfficeRightToPublicFundsConnectorSpec extends AnyWordSpecLike with Matchers {

  import HomeOfficeRightToPublicFundsConnector.*

  private lazy val mockAppConfig: AppConfig     = mock(classOf[AppConfig])
  private lazy val mockHttpClient: HttpClientV2 = mock(classOf[HttpClientV2])

  trait Setup {
    val uuid = "123f4567-g89c-42c3-b456-557742330000"
    val connector: HomeOfficeRightToPublicFundsConnector =
      new HomeOfficeRightToPublicFundsConnector(mockAppConfig, mockHttpClient)(global) {
        override def generateNewUUID: String = uuid
      }
  }

  "requestID is present in the headerCarrier" should {
    "return new ID pre-appending the requestID when the requestID matches the format(8-4-4-4)" in new Setup {
      val requestId  = "dcba0000-ij12-df34-jk56"
      val uuidLength = 24
      connector.correlationId(Some(RequestId(requestId))) shouldBe s"$requestId-${uuid.substring(uuidLength)}"
    }

    "return new ID when the requestID does not match the format(8-4-4-4)" in new Setup {
      val requestId = "1a2b-ij12-df34-jk56"
      connector.correlationId(Some(RequestId(requestId))) shouldBe uuid
    }
  }

  "requestID is not present in the headerCarrier should return a new ID" should {
    "return the uuid" in new Setup {
      connector.correlationId(None) shouldBe uuid
    }
  }

  "buildURL" should {
    "throw an exception when invalid URL" in new Setup {
      intercept[IllegalArgumentException](
        buildURL("test", "/", "/")
      ) shouldBe a[IllegalArgumentException]
    }

    "return the correct URL, when base has no suffix" in new Setup {
      buildURL(
        "/test",
        "http://localhost:1234",
        "/prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

    "return the correct URL, when base has suffix" in new Setup {
      buildURL(
        "/test",
        "http://localhost:1234/",
        "/prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

    "return the correct URL, when middle has no prefix" in new Setup {
      buildURL(
        "/test",
        "http://localhost:1234/",
        "prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

    "return the correct URL, when middle has prefix" in new Setup {
      buildURL(
        "/test",
        "http://localhost:1234/",
        "/prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

    "return the correct URL, when path has no prefix" in new Setup {
      buildURL(
        "test",
        "http://localhost:1234/",
        "/prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

    "return the correct URL, when path has prefix" in new Setup {
      buildURL(
        "/test",
        "http://localhost:1234/",
        "/prefix"
      ).toString shouldBe "http://localhost:1234/prefix/test"
    }

  }

}
