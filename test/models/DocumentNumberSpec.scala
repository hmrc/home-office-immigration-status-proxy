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
import cats.data.Validated.*
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import models.DocumentNumber.*

class DocumentNumberSpec extends SpecBase {

  "apply" must {
    "return a failed validation" when {
      "the string is empty" in {
        DocumentNumber("") mustBe Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }

      "the string is shorter than 3 chars" in {
        DocumentNumber("ABCDEFGHIJABCDEFGHIJABCDEFGHIJ1") mustBe Invalid(
          Chain(ErrorMessage("Document number must be between 1 and 30 characters"))
        )
      }

      "the string contains lower case" in {
        DocumentNumber("ABC-123-abc") mustBe Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }

      "the string contains non alpha (or hyphen) chars" in {
        DocumentNumber("ABC-123-???") mustBe Invalid(
          Chain(ErrorMessage("Document number must only contain upper case alphanumeric characters or hyphens"))
        )
      }
    }

    "return a successful validation" when {
      "the string is 1 alpha char" in {
        DocumentNumber("A") mustBe a[Valid[?]]
        DocumentNumber("A").map(_.doc mustBe "A")
      }

      "the string is 1 num char" in {
        DocumentNumber("1") mustBe a[Valid[?]]
        DocumentNumber("1").map(_.doc mustBe "1")
      }

      "the string is 1 hyphen" in {
        DocumentNumber("-") mustBe a[Valid[?]]
        DocumentNumber("-").map(_.doc mustBe "-")
      }

      "the string is a mixture of numbers, hyphen and alphas" in {
        DocumentNumber("123-ABC") mustBe a[Valid[?]]
        DocumentNumber("123-ABC").map(_.doc mustBe "123-ABC")
      }

      "the string is 30 chars" in {
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-") mustBe a[Valid[?]]
        DocumentNumber("ABCDEFGHIJ1234567890ABCDEFGHI-").map(_.doc mustBe "ABCDEFGHIJ1234567890ABCDEFGHI-")
      }
    }
  }

  "reads" must {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("12345.")

        jsString.validate[DocumentNumber] mustBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString("12345")

        jsString.validate[DocumentNumber] mustBe a[JsSuccess[?]]
      }
    }
  }

  "writes" must {
    "return a JsString" in {
      DocumentNumber("ABCDE").map(doc => Json.toJson(doc) mustBe JsString("ABCDE"))
    }
  }

}
