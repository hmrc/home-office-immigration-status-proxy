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
import java.time.LocalDate

class DateOfBirthSpec extends AnyWordSpecLike with Matchers {

  "apply" should {
    "return a failed validation" when {
      "the date is today" in {
        DateOfBirth(LocalDate.now) shouldEqual Invalid(Chain(ErrorMessage("Date of birth must be before today")))
      }
      "the date is after today" in {
        DateOfBirth(LocalDate.now.plusDays(1)) shouldEqual Invalid(
          Chain(ErrorMessage("Date of birth must be before today"))
        )
      }
    }
    "return a successful validation" when {
      "the string is 3 chars" in {
        DateOfBirth(LocalDate.now.minusDays(1)) shouldBe a[Valid[_]]
        DateOfBirth(LocalDate.now.minusDays(1)).map(_.dob shouldEqual LocalDate.now.minusDays(1))
      }
    }
  }

  "reads" should {
    "return a JsError" when {
      "the apply returns a failure" in {
        val jsString = JsString(LocalDate.now.plusDays(1).toString)
        jsString.validate[DateOfBirth] shouldBe a[JsError]
      }
    }

    "return a JsSuccess" when {
      "the apply returns a success" in {
        val jsString = JsString(LocalDate.now.minusDays(1).toString)
        jsString.validate[DateOfBirth] shouldBe a[JsSuccess[_]]
      }
    }
  }

  "writes" should {
    "return a JsString" in {
      DateOfBirth(LocalDate.now.minusDays(1)).map(doc =>
        Json.toJson(doc) shouldEqual JsString(LocalDate.now.minusDays(1).toString)
      )
    }
  }

}
