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

package models

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}
import models.OAuthToken.*

class OAuthTokenSpec extends SpecBase {

  "OAuthToken" must {
    "serialize to JSON" when {
      "all fields are defined" in {
        Json.toJson(oAuthToken) mustBe Json.obj(
          "access_token" -> tokenId,
          "token_type"   -> tokenType
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "access_token" -> tokenId,
          "token_type"   -> tokenType
        )
        json.validate[OAuthToken] mustBe JsSuccess(oAuthToken)
      }

      "missing fields" in {
        val json = Json.obj(
          "access_token" -> tokenId,
          "blah"         -> "blah"
        )
        json.validate[OAuthToken] mustBe a[JsError]
      }

      "invalid field types" in {
        val json = Json.obj(
          "access_token" -> 12345,
          "token_type"   -> tokenType
        )
        json.validate[OAuthToken] mustBe a[JsError]
      }
    }
  }
}
