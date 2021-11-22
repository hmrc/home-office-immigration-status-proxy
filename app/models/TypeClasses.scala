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

import play.api.libs.json.{JsError, JsPath, JsonValidationError, Reads}
import models.ErrorMessage._
import cats.data.Validated._

object TypeClasses {
  implicit class RichReadsValidationResult[A](reads: Reads[ValidationResult[A]]) {
    def flattenValidated: Reads[A] = reads.flatMap {
      case Invalid(errors) =>
        val validationErrors: List[JsonValidationError] =
          errors.map(e => JsonValidationError(e.message)).toChain.toList
        Reads(_ => JsError(Seq(JsPath -> validationErrors)))
      case Valid(value) => Reads.pure(value)
    }
  }
//   implicit class RichReadsValidationResult[A](reads: Reads[ValidatedNec[ErrorMessage, A]]) {
//     def flattenEither: Reads[A] = reads.flatMap {
//       case Invalid(error) => Reads.failed(error)
//       case Right(value)   => Reads.pure(value)
//     }
//   }
}
