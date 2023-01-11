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

package models

import cats.data.Chain
import cats.data.Validated._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class NationalitySpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the string is empty" in {
        Nationality("") shouldEqual Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }
      "the string is shorter than 3 chars" in {
        Nationality("AB") shouldEqual Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }
      "the string is longer than 3 chars" in {
        Nationality("ABVD") shouldEqual Invalid(Chain(ErrorMessage("Nationality needs to be 3 characters long")))
      }
    }
    "return a successful validation" when {
      "the string is 3 chars" in {
        Nationality("ABC") shouldBe a[Valid[_]]
        Nationality("ABC").map(_.nationality shouldEqual "ABC")
      }
    }
  }

  "reads" should {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("ABCD")
        jsString.validate[Nationality] shouldBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString("ABC")
        jsString.validate[Nationality] shouldBe a[JsSuccess[_]]
      }
    }
  }

  "writes" should {
    "return a JsString" in {
      Nationality("ABC").map(doc => Json.toJson(doc) shouldEqual JsString("ABC"))
    }
  }

}
