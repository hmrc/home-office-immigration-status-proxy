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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsSuccess, Json}
import models.OAuthToken.*

class OAuthTokenSpec extends AnyWordSpecLike with Matchers {

  "OAuthToken" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val token = OAuthToken("access_token_value", "Bearer")
        Json.toJson(token) shouldBe Json.obj(
          "access_token" -> "access_token_value",
          "token_type"   -> "Bearer"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "access_token" -> "access_token_value",
          "token_type"   -> "Bearer"
        )
        json.validate[OAuthToken] shouldBe JsSuccess(OAuthToken("access_token_value", "Bearer"))
      }

      "missing fields" in {
        val json = Json.obj(
          "accessToken" -> "access_token_value",
          "tokenType"   -> "Bearer"
        )
        json.validate[OAuthToken] shouldBe a[JsError]
      }

      "invalid field types" in {
        val json = Json.obj(
          "access_token" -> 12345,
          "token_type"   -> "Bearer"
        )
        json.validate[OAuthToken] shouldBe a[JsError]
      }
    }
  }
}
