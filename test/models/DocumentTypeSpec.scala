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
import models.DocumentType.*

class DocumentTypeSpec extends SpecBase {

  "apply" must {
    "return a failed validation" when {
      "the string is not an allowed value" in {
        DocumentType("ABC") mustBe Invalid(Chain(ErrorMessage("Document type must be PASSPORT, NAT, BRC, or BRP")))
      }
    }

    "return a successful validation" when {
      "type is passport" in {
        DocumentType("PASSPORT") mustBe a[Valid[?]]
        DocumentType("PASSPORT").map(_ mustBe DocumentType.Passport)
      }

      "type is NAT" in {
        DocumentType("NAT") mustBe a[Valid[?]]
        DocumentType("NAT").map(_ mustBe DocumentType.EUNationalID)
      }

      "type is BRC" in {
        DocumentType("BRC") mustBe a[Valid[?]]
        DocumentType("BRC").map(_ mustBe DocumentType.BRC)
      }

      "type is BRP" in {
        DocumentType("BRP") mustBe a[Valid[?]]
        DocumentType("BRP").map(_ mustBe DocumentType.BRP)
      }
    }
  }

  "reads" must {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("ABC")

        jsString.validate[DocumentType] mustBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString("PASSPORT")

        jsString.validate[DocumentType] mustBe a[JsSuccess[?]]
      }
    }
  }

  "writes" must {
    "return a JsString" when {
      "type is passport" in {
        val documentType: DocumentType = DocumentType.Passport

        Json.toJson(documentType) mustBe JsString("PASSPORT")
      }

      "type is EUNationalID" in {
        val documentType: DocumentType = DocumentType.EUNationalID

        Json.toJson(documentType) mustBe JsString("NAT")
      }

      "type is BRC" in {
        val documentType: DocumentType = DocumentType.BRC

        Json.toJson(documentType) mustBe JsString("BRC")
      }

      "type is BRP" in {
        val documentType: DocumentType = DocumentType.BRP

        Json.toJson(documentType) mustBe JsString("BRP")
      }
    }
  }

}
