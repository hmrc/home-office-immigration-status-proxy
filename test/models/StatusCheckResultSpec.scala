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
import play.api.libs.json.{JsError, JsSuccess, Json}
import models.StatusCheckResult.*
import models.ImmigrationStatus.*

import java.time.LocalDate

class StatusCheckResultSpec extends SpecBase {

  private val status = ImmigrationStatus(
    LocalDate.parse(statusStartDate),
    Some(LocalDate.parse(statusEndDate)),
    productType,
    immigrationStatus,
    noRecourseToPublicFunds = noRecourseToPublicFunds
  )

  "StatusCheckResult" must {
    "serialize to JSON" when {
      "all fields are defined" in {
        val result = StatusCheckResult(
          fullName,
          LocalDate.parse(dob),
          nationality,
          List(status)
        )

        Json.toJson(result) mustBe Json.obj(
          "fullName"    -> fullName,
          "dateOfBirth" -> dob,
          "nationality" -> nationality,
          "statuses"    -> Json.arr(Json.toJson(status))
        )
      }

      "statuses are empty" in {
        val result = StatusCheckResult(
          fullName,
          LocalDate.parse(dob),
          nationality,
          List.empty
        )

        Json.toJson(result) mustBe Json.obj(
          "fullName"    -> fullName,
          "dateOfBirth" -> dob,
          "nationality" -> nationality,
          "statuses"    -> Json.arr()
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "fullName"    -> fullName,
          "dateOfBirth" -> dob,
          "nationality" -> nationality,
          "statuses"    -> Json.arr(Json.toJson(status))
        )

        json.validate[StatusCheckResult] mustBe JsSuccess(
          StatusCheckResult(
            fullName,
            LocalDate.parse(dob),
            nationality,
            List(status)
          )
        )
      }

      "statuses are empty" in {
        val json = Json.obj(
          "fullName"    -> fullName,
          "dateOfBirth" -> dob,
          "nationality" -> nationality,
          "statuses"    -> Json.arr()
        )

        json.validate[StatusCheckResult] mustBe JsSuccess(
          StatusCheckResult(
            fullName,
            LocalDate.parse(dob),
            nationality,
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

        json.validate[StatusCheckResult] mustBe a[JsError]
      }
    }
  }
}
