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

package helpers

import base.SpecBase
import uk.gov.hmrc.http.RequestId

class CorrelationIdHelperSpec extends SpecBase {
  "correlationId" must {
    "return new ID pre-appending the requestID when the requestID matches the format(8-4-4-4)" in {
      val requestId = "dcba0000-ij12-df34-jk56"
      val result    = CorrelationIdHelper.correlationId(Some(RequestId(requestId)))

      result.startsWith(requestId) mustBe true
      result.length() mustBe requestId.length() + 13
    }

    "return new ID when the requestID does not match the format(8-4-4-4)" in {
      val requestId = "1a2b-ij12-df34-jk56"
      val result    = CorrelationIdHelper.correlationId(Some(RequestId(requestId)))

      result.length() mustBe 36
    }

    "return a new ID when there is no requestID" in {
      val result = CorrelationIdHelper.correlationId(None)

      result.length() mustBe 36
    }
  }
}
