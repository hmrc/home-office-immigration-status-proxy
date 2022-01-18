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

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import models.ErrorMessage._
import cats.implicits._

final case class DocumentNumber private (val doc: String) extends AnyVal
object DocumentNumber {
  def apply(doc: String): ValidationResult[DocumentNumber] =
    (
      validateLength(doc),
      validateCharacters(doc)
    ).mapN { case _ => new DocumentNumber(doc) }

  private def validateLength(doc: String): ValidationResult[String] =
    if (doc.length > 0 && doc.length < 31) doc.validNec
    else ErrorMessage("Document number must be between 1 and 30 characters").invalidNec

  private def validateCharacters(doc: String): ValidationResult[String] = {
    val regex = "^[0-9A-Z-]*$".r
    doc match {
      case regex(_*) => doc.validNec
      case _ =>
        ErrorMessage(
          "Document number must only contain upper case alphanumeric characters or hyphens").invalidNec
    }
  }

  implicit lazy val reads: Reads[DocumentNumber] =
    (JsPath).read[String].map(DocumentNumber.apply).flattenValidated
  implicit lazy val writes: Writes[DocumentNumber] = Json.valueWrites[DocumentNumber]
}
