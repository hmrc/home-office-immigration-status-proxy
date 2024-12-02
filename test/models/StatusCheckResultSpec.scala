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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsSuccess, Json}
import models.StatusCheckResult.*
import models.ImmigrationStatus.*

import java.time.LocalDate

class StatusCheckResultSpec extends AnyWordSpecLike with Matchers {

  private val status = ImmigrationStatus(
    LocalDate.of(2020, 1, 1),
    Some(LocalDate.of(2024, 12, 31)),
    "ProductType1",
    "Status1",
    noRecourseToPublicFunds = true
  )

  "StatusCheckResult" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val result = StatusCheckResult(
          "John Doe",
          LocalDate.of(1990, 1, 1),
          "British",
          List(status)
        )
        Json.toJson(result) shouldBe Json.obj(
          "fullName"    -> "John Doe",
          "dateOfBirth" -> "1990-01-01",
          "nationality" -> "British",
          "statuses"    -> Json.arr(Json.toJson(status))
        )
      }

      "statuses are empty" in {
        val result = StatusCheckResult(
          "John Doe",
          LocalDate.of(1990, 1, 1),
          "British",
          List.empty
        )
        Json.toJson(result) shouldBe Json.obj(
          "fullName"    -> "John Doe",
          "dateOfBirth" -> "1990-01-01",
          "nationality" -> "British",
          "statuses"    -> Json.arr()
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "fullName"    -> "John Doe",
          "dateOfBirth" -> "1990-01-01",
          "nationality" -> "British",
          "statuses"    -> Json.arr(Json.toJson(status))
        )
        json.validate[StatusCheckResult] shouldBe JsSuccess(
          StatusCheckResult(
            "John Doe",
            LocalDate.of(1990, 1, 1),
            "British",
            List(status)
          )
        )
      }

      "statuses are empty" in {
        val json = Json.obj(
          "fullName"    -> "John Doe",
          "dateOfBirth" -> "1990-01-01",
          "nationality" -> "British",
          "statuses"    -> Json.arr()
        )
        json.validate[StatusCheckResult] shouldBe JsSuccess(
          StatusCheckResult(
            "John Doe",
            LocalDate.of(1990, 1, 1),
            "British",
            List.empty
          )
        )
      }

      "invalid field types" in {
        val json = Json.obj(
          "fullName"    -> 12345,
          "dateOfBirth" -> true,
          "nationality" -> 67890,
          "statuses"    -> "Invalid"
        )
        json.validate[StatusCheckResult] shouldBe a[JsError]
      }
    }
  }
}
