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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cats.data.Validated._
import cats.data.Chain
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class DocumentTypeSpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the string is not an allowed value" in {
        DocumentType("ABC") shouldEqual Invalid(Chain(ErrorMessage("Document type must be PASSPORT, NAT, BRC, or BRP")))
      }
    }
    "return a successful validation" when {
      "type is passport" in {
        DocumentType("PASSPORT") shouldBe a[Valid[_]]
        DocumentType("PASSPORT").map(_ shouldEqual DocumentType.Passport)
      }
      "type is NAT" in {
        DocumentType("NAT") shouldBe a[Valid[_]]
        DocumentType("NAT").map(_ shouldEqual DocumentType.EUNationalID)
      }
      "type is BRC" in {
        DocumentType("BRC") shouldBe a[Valid[_]]
        DocumentType("BRC").map(_ shouldEqual DocumentType.BRC)
      }
      "type is BRP" in {
        DocumentType("BRP") shouldBe a[Valid[_]]
        DocumentType("BRP").map(_ shouldEqual DocumentType.BRP)
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
        jsString.validate[DocumentType] shouldBe a[JsSuccess[_]]
      }
    }
  }

  "writes" should {
    "return a JsString" when {
      "type is passport" in {
        val documentType: DocumentType = DocumentType.Passport
        Json.toJson(documentType) shouldEqual JsString("PASSPORT")
      }
      "type is EUNationalID" in {
        val documentType: DocumentType = DocumentType.EUNationalID
        Json.toJson(documentType) shouldEqual JsString("NAT")
      }
      "type is BRC" in {
        val documentType: DocumentType = DocumentType.BRC
        Json.toJson(documentType) shouldEqual JsString("BRC")
      }
      "type is BRP" in {
        val documentType: DocumentType = DocumentType.BRP
        Json.toJson(documentType) shouldEqual JsString("BRP")
      }
    }
  }

}
