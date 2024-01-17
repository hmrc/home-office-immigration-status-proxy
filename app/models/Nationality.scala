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

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import models.ErrorMessage._
import cats.implicits._

final case class Nationality private (val nationality: String) extends AnyVal
object Nationality {
  def apply(nationality: String): ValidationResult[Nationality] =
    validateLength(nationality).map(new Nationality(_))

  private def validateLength(nationality: String): ValidationResult[String] =
    if (nationality.length == 3) {
      nationality.validNec
    } else {
      ErrorMessage("Nationality needs to be 3 characters long").invalidNec
    }

  implicit lazy val reads: Reads[Nationality] =
    JsPath.read[String].map(Nationality.apply).flattenValidated
  implicit lazy val writes: Writes[Nationality] = Json.valueWrites[Nationality]
}
