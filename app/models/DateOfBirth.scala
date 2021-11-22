/*
 * Copyright 2021 HM Revenue & Customs
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
import models.TypeClasses._
import models.ErrorMessage._
import cats.implicits._
import java.time.LocalDate

final case class DateOfBirth private (val dob: LocalDate) extends AnyVal
object DateOfBirth {
  def apply(dob: LocalDate): ValidationResult[DateOfBirth] =
    validateDate(dob).map(new DateOfBirth(_))

  def validateDate(dob: LocalDate): ValidationResult[LocalDate] =
    if (dob.isBefore(LocalDate.now)) dob.validNec
    else ErrorMessage("Date of birth must be before today").invalidNec

  implicit lazy val reads: Reads[DateOfBirth] =
    (JsPath).read[LocalDate].map(DateOfBirth.apply).flattenValidated
  implicit lazy val writes: Writes[DateOfBirth] = Json.valueWrites[DateOfBirth]
}
