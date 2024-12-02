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

import cats.data.Chain
import cats.data.Validated._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import models.DocumentNumber.*

class DocumentNumberSpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the string is empty" in {
        DocumentNumber("") shouldBe Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }
      "the string is shorter than 3 chars" in {
        DocumentNumber("ABCDEFGHIJABCDEFGHIJABCDEFGHIJ1") shouldBe Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }
      "the string contains lower case" in {
        DocumentNumber("ABC-123-abc") shouldBe Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }
      "the string contains non alpha (or hyphen) chars" in {
        DocumentNumber("ABC-123-???") shouldBe Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }
    }
    "return a successful validation" when {
      "the string is 1 alpha char" in {
        DocumentNumber("A") shouldBe a[Valid[?]]
        DocumentNumber("A").map(_.doc shouldBe "A")
      }
      "the string is 1 num char" in {
        DocumentNumber("1") shouldBe a[Valid[?]]
        DocumentNumber("1").map(_.doc shouldBe "1")
      }
      "the string is 1 hyphen" in {
        DocumentNumber("-") shouldBe a[Valid[?]]
        DocumentNumber("-").map(_.doc shouldBe "-")
      }
      "the string is a mixture" in {
        DocumentNumber("123-ABC") shouldBe a[Valid[?]]
        DocumentNumber("123-ABC").map(_.doc shouldBe "123-ABC")
      }
      "the string is 30 chars" in {
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-") shouldBe a[Valid[?]]
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-").map(_.doc shouldBe "ABCDEFGHIJ1234567890ABCDEFGHI-")
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
        jsString.validate[DocumentNumber] shouldBe a[JsSuccess[?]]
      }
    }
  }

  "writes" should {
    "return a JsString" in {
      DocumentNumber("ABCDE").map(doc => Json.toJson(doc) shouldBe JsString("ABCDE"))
    }
  }

}
