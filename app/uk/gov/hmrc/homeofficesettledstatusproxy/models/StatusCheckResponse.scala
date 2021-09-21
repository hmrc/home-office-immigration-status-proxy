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

package uk.gov.hmrc.homeofficesettledstatusproxy.models

import play.api.libs.json.{Format, Json}

case class StatusCheckResponse(
  // Client provided or system generated transaction id
  correlationId: String,
  // Represents an error occurred
  error: Option[StatusCheckError] = None,
  // Represents the result
  result: Option[StatusCheckResult] = None
)

object StatusCheckResponse {
  implicit val formats: Format[StatusCheckResponse] = Json.format[StatusCheckResponse]

  def error(correlationId: String, errCode: String, fields: Option[List[(String, String)]] = None) =
    StatusCheckResponse(
      correlationId = correlationId,
      error = Some(
        StatusCheckError(
          errCode = errCode,
          fields = fields.map(f => f.map { case (code, name) => ValidationError(code, name) })))
    )

  object HasResult {
    def unapply(response: StatusCheckResponse): Option[StatusCheckResponse] =
      if (response.error.isDefined) None else Some(response)
  }

  object HasError {
    def unapply(response: StatusCheckResponse): Option[(String, StatusCheckResponse)] =
      response.error.map(_.errCode).map(errCode => (errCode, response))
  }
}
