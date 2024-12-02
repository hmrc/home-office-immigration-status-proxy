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
import models.DocumentType.*

class DocumentTypeSpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the string is not an allowed value" in {
        DocumentType("ABC") shouldBe Invalid(Chain(ErrorMessage("Document type must be PASSPORT, NAT, BRC, or BRP")))
      }
    }
    "return a successful validation" when {
      "type is passport" in {
        DocumentType("PASSPORT") shouldBe a[Valid[?]]
        DocumentType("PASSPORT").map(_ shouldBe DocumentType.Passport)
      }
      "type is NAT" in {
        DocumentType("NAT") shouldBe a[Valid[?]]
        DocumentType("NAT").map(_ shouldBe DocumentType.EUNationalID)
      }
      "type is BRC" in {
        DocumentType("BRC") shouldBe a[Valid[?]]
        DocumentType("BRC").map(_ shouldBe DocumentType.BRC)
      }
      "type is BRP" in {
        DocumentType("BRP") shouldBe a[Valid[?]]
        DocumentType("BRP").map(_ shouldBe DocumentType.BRP)
      }
    }
  }

  "reads" should {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString("ABC")
        jsString.validate[DocumentType] shouldBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString("PASSPORT")
        jsString.validate[DocumentType] shouldBe a[JsSuccess[?]]
      }
    }
  }

  "writes" should {
    "return a JsString" when {
      "type is passport" in {
        val documentType: DocumentType = DocumentType.Passport
        Json.toJson(documentType) shouldBe JsString("PASSPORT")
      }
      "type is EUNationalID" in {
        val documentType: DocumentType = DocumentType.EUNationalID
        Json.toJson(documentType) shouldBe JsString("NAT")
      }
      "type is BRC" in {
        val documentType: DocumentType = DocumentType.BRC
        Json.toJson(documentType) shouldBe JsString("BRC")
      }
      "type is BRP" in {
        val documentType: DocumentType = DocumentType.BRP
        Json.toJson(documentType) shouldBe JsString("BRP")
      }
    }
  }

}
