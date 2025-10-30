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

package connectors

import connectors.ErrorCodes.{ERR_HOME_OFFICE_RESPONSE, ERR_UNKNOWN}
import models.{StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import wiring.Constants.*

import scala.util.{Failure, Success, Try}

object StatusCheckResponseHttpParser extends Logging {

  implicit object StatusCheckResponseReads
      extends HttpReads[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]] {

    override def read(
      method: String,
      url: String,
      response: HttpResponse
    ): Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse] =
      response.status match {
        case OK =>
          Try(response.json.as[StatusCheckResponse]) match {
            case Success(res) =>
              Right(res)
            case Failure(e) =>
              logger.error(s"[StatusCheckResponseHttpParser][read] Invalid json returned in response", e)
              val correlationId = response.header(HEADER_X_CORRELATION_ID)
              Left(jsonParsingError(correlationId))
          }
        case _ =>
          Try(response.json.as[StatusCheckErrorResponse]) match {
            case Success(errorResponse) =>
              logger.error(
                s"[StatusCheckResponseHttpParser][read] Home office returned ${response.status} error ${Json.toJson(errorResponse.error)}"
              )
              Left(StatusCheckErrorResponseWithStatus(response.status, errorResponse))
            case Failure(_) =>
              logger.error(
                s"[StatusCheckResponseHttpParser][read] Unable to parse the error response returned by Home office. Status: $response.status"
              )
              val correlationId = response.header(HEADER_X_CORRELATION_ID)
              Left(unknownError(response.status, correlationId))
          }
      }
  }

  private def jsonParsingError(correlationId: Option[String]): StatusCheckErrorResponseWithStatus =
    StatusCheckErrorResponseWithStatus(
      INTERNAL_SERVER_ERROR,
      StatusCheckErrorResponse(correlationId, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
    )

  private def unknownError(status: Int, correlationId: Option[String]): StatusCheckErrorResponseWithStatus =
    StatusCheckErrorResponseWithStatus(status, StatusCheckErrorResponse(correlationId, StatusCheckError(ERR_UNKNOWN)))

}
