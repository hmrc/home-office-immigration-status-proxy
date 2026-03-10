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
import java.time.LocalDate
import models.ImmigrationStatus.*

class ImmigrationStatusSpec extends SpecBase {

  "ImmigrationStatus" must {
    "serialize to JSON" when {
      "all fields are defined" in {
        val status = ImmigrationStatus(
          LocalDate.parse(statusStartDate),
          Some(LocalDate.parse(statusEndDate)),
          productType,
          immigrationStatus,
          noRecourseToPublicFunds = noRecourseToPublicFunds
        )

        Json.toJson(status) mustBe Json.obj(
          "statusStartDate"         -> statusStartDate,
          "statusEndDate"           -> statusEndDate,
          "productType"             -> productType,
          "immigrationStatus"       -> immigrationStatus,
          "noRecourseToPublicFunds" -> noRecourseToPublicFunds
        )
      }

      "statusEndDate is empty" in {
        val status = ImmigrationStatus(
          LocalDate.parse(statusStartDate),
          None,
          productType,
          immigrationStatus,
          noRecourseToPublicFunds = noRecourseToPublicFunds
        )

        Json.toJson(status) mustBe Json.obj(
          "statusStartDate"         -> statusStartDate,
          "productType"             -> productType,
          "immigrationStatus"       -> immigrationStatus,
          "noRecourseToPublicFunds" -> noRecourseToPublicFunds
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "statusStartDate"         -> statusStartDate,
          "statusEndDate"           -> statusEndDate,
          "productType"             -> productType,
          "immigrationStatus"       -> immigrationStatus,
          "noRecourseToPublicFunds" -> noRecourseToPublicFunds
        )

        json.validate[ImmigrationStatus] mustBe JsSuccess(
          ImmigrationStatus(
            LocalDate.parse(statusStartDate),
            Some(LocalDate.parse(statusEndDate)),
            productType,
            immigrationStatus,
            noRecourseToPublicFunds = noRecourseToPublicFunds
          )
        )
      }

      "statusEndDate is empty" in {
        val json = Json.obj(
          "statusStartDate"         -> statusStartDate,
          "productType"             -> productType,
          "immigrationStatus"       -> immigrationStatus,
          "noRecourseToPublicFunds" -> noRecourseToPublicFunds
        )

        json.validate[ImmigrationStatus] mustBe JsSuccess(
          ImmigrationStatus(
            LocalDate.parse(statusStartDate),
            None,
            productType,
            immigrationStatus,
            noRecourseToPublicFunds = noRecourseToPublicFunds
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

        json.validate[ImmigrationStatus] mustBe a[JsError]
      }
    }
  }
}
