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
import java.time.LocalDate
import models.ImmigrationStatus.*

class ImmigrationStatusSpec extends AnyWordSpecLike with Matchers {

  "ImmigrationStatus" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val status = ImmigrationStatus(
          LocalDate.of(2020, 1, 1),
          Some(LocalDate.of(2024, 12, 31)),
          "ProductType1",
          "Status1",
          noRecourseToPublicFunds = true
        )
        Json.toJson(status) shouldBe Json.obj(
          "statusStartDate"         -> "2020-01-01",
          "statusEndDate"           -> "2024-12-31",
          "productType"             -> "ProductType1",
          "immigrationStatus"       -> "Status1",
          "noRecourseToPublicFunds" -> true
        )
      }

      "statusEndDate is empty" in {
        val status = ImmigrationStatus(
          LocalDate.of(2020, 1, 1),
          None,
          "ProductType1",
          "Status1",
          noRecourseToPublicFunds = true
        )
        Json.toJson(status) shouldBe Json.obj(
          "statusStartDate"         -> "2020-01-01",
          "productType"             -> "ProductType1",
          "immigrationStatus"       -> "Status1",
          "noRecourseToPublicFunds" -> true
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "statusStartDate"         -> "2020-01-01",
          "statusEndDate"           -> "2024-12-31",
          "productType"             -> "ProductType1",
          "immigrationStatus"       -> "Status1",
          "noRecourseToPublicFunds" -> true
        )
        json.validate[ImmigrationStatus] shouldBe JsSuccess(
          ImmigrationStatus(
            LocalDate.of(2020, 1, 1),
            Some(LocalDate.of(2024, 12, 31)),
            "ProductType1",
            "Status1",
            noRecourseToPublicFunds = true
          )
        )
      }

      "statusEndDate is empty" in {
        val json = Json.obj(
          "statusStartDate"         -> "2020-01-01",
          "productType"             -> "ProductType1",
          "immigrationStatus"       -> "Status1",
          "noRecourseToPublicFunds" -> true
        )
        json.validate[ImmigrationStatus] shouldBe JsSuccess(
          ImmigrationStatus(
            LocalDate.of(2020, 1, 1),
            None,
            "ProductType1",
            "Status1",
            noRecourseToPublicFunds = true
          )
        )
      }

      "invalid field types" in {
        val json = Json.obj(
          "statusStartDate"         -> 12345,
          "statusEndDate"           -> true,
          "productType"             -> 67890,
          "immigrationStatus"       -> false,
          "noRecourseToPublicFunds" -> "Invalid"
        )
        json.validate[ImmigrationStatus] shouldBe a[JsError]
      }
    }
  }
}
