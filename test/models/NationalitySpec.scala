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
import cats.data.Chain
import cats.data.Validated._
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import models.Nationality._

class NationalitySpec extends SpecBase {

  "apply" must {
    "return a failed validation" when {
      "the string is empty" in {
        Nationality("") mustBe Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }

      "the string is shorter than 3 chars" in {
        Nationality("AB") mustBe Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }

      "the string is longer than 3 chars" in {
        Nationality("ABVD") mustBe Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }
    }
    "return a successful validation" when {
      "the string is 3 chars" in {
        Nationality("ABC") mustBe a[Valid[?]]
        Nationality("ABC").map(_.nationality mustBe "ABC")
      }
    }
  }

  "reads" must {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("ABCD")

        jsString.validate[Nationality] mustBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString(nationality)

        jsString.validate[Nationality] mustBe a[JsSuccess[?]]
      }
    }
  }

  "writes" must {
    "return a JsString" in {
      Nationality(nationality).map(doc => Json.toJson(doc) mustBe JsString(nationality))
    }
  }

}
