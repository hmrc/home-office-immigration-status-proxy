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

class ValidationErrorSpec extends SpecBase {

  "ValidationError" must {
    "serialize to JSON" when {
      "all fields are defined" in {
        val error = ValidationError("field1", "error1")

        Json.toJson(error) mustBe Json.obj(
          "name" -> "field1",
          "code" -> "error1"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "name" -> "field1",
          "code" -> "error1"
        )

        json.validate[ValidationError] mustBe JsSuccess(ValidationError("field1", "error1"))
      }

      "invalid field types" in {
        val json = Json.obj(
          "name" -> 12345,
          "code" -> true
        )

        json.validate[ValidationError] mustBe a[JsError]
      }
    }
  }
}
