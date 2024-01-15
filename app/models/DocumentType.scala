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

import play.api.libs.json.{JsPath, JsString, Reads, Writes}
import models.ErrorMessage._
import cats.implicits._

sealed abstract class DocumentType(val code: String)
object DocumentType {
  case object Passport extends DocumentType("PASSPORT")
  case object EUNationalID extends DocumentType("NAT")
  case object BRC extends DocumentType("BRC")
  case object BRP extends DocumentType("BRP")

  def apply(docType: String): ValidationResult[DocumentType] = docType match {
    case "PASSPORT" => Passport.validNec
    case "NAT"      => EUNationalID.validNec
    case "BRC"      => BRC.validNec
    case "BRP"      => BRP.validNec
    case _          => ErrorMessage("Document type must be PASSPORT, NAT, BRC, or BRP").invalidNec
  }

  implicit lazy val reads: Reads[DocumentType] =
    JsPath.read[String].map(DocumentType.apply).flattenValidated
  implicit lazy val writes: Writes[DocumentType] = Writes { model =>
    JsString(model.code)
  }
}
