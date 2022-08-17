/*
 * Copyright 2022 HM Revenue & Customs
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
import cats.data.Validated._
import cats.data.Chain
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class DocumentNumberSpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the string is empty" in {
        DocumentNumber("") shouldEqual Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }
      "the string is shorter than 3 chars" in {
        DocumentNumber("ABCDEFGHIJABCDEFGHIJABCDEFGHIJ1") shouldEqual Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }
      "the string contains lower case" in {
        DocumentNumber("ABC-123-abc") shouldEqual Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }
      "the string contains non alpha (or hyphen) chars" in {
        DocumentNumber("ABC-123-???") shouldEqual Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }
    }
    "return a successful validation" when {
      "the string is 1 alpha char" in {
        DocumentNumber("A") shouldBe a[Valid[_]]
        DocumentNumber("A").map(_.doc shouldEqual "A")
      }
      "the string is 1 num char" in {
        DocumentNumber("1") shouldBe a[Valid[_]]
        DocumentNumber("1").map(_.doc shouldEqual "1")
      }
      "the string is 1 hyphen" in {
        DocumentNumber("-") shouldBe a[Valid[_]]
        DocumentNumber("-").map(_.doc shouldEqual "-")
      }
      "the string is a mixture" in {
        DocumentNumber("123-ABC") shouldBe a[Valid[_]]
        DocumentNumber("123-ABC").map(_.doc shouldEqual "123-ABC")
      }
      "the string is 30 chars" in {
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-") shouldBe a[Valid[_]]
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-").map(_.doc shouldEqual "ABCDEFGHIJ1234567890ABCDEFGHI-")
      }
    }
  }

  "reads" should {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("12345.")
        jsString.validate[DocumentNumber] shouldBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString("12345")
        jsString.validate[DocumentNumber] shouldBe a[JsSuccess[_]]
      }
    }
  }

  "writes" should {
    "return a JsString" in {
      DocumentNumber("ABCDE").map(doc => Json.toJson(doc) shouldEqual JsString("ABCDE"))
    }
  }

}
