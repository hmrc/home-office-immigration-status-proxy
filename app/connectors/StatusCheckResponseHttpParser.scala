/*
 * Copyright 2022 HM Revenue & Customs
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

import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._
import play.api.Logging
import scala.util.{Failure, Success, Try}
import models.{StatusCheckError, StatusCheckErrorResponse, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import connectors.ErrorCodes._
import wiring.Constants._

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
              logger.error(s"Invalid json returned in response", e)
              val correlationId = response.header(HEADER_X_CORRELATION_ID)
              Left(jsonParsingError(correlationId))
          }
        case _ =>
          Try(response.json.as[StatusCheckErrorResponse]) match {
            case Success(errorResponse) =>
              Left(StatusCheckErrorResponseWithStatus(response.status, errorResponse))
            case Failure(_) =>
              val correlationId = response.header(HEADER_X_CORRELATION_ID)
              Left(unknownError(response.status, correlationId))
          }
      }
  }

  def jsonParsingError(correlationId: Option[String]): StatusCheckErrorResponseWithStatus =
    StatusCheckErrorResponseWithStatus(
      INTERNAL_SERVER_ERROR,
      StatusCheckErrorResponse(correlationId, StatusCheckError(ERR_HOME_OFFICE_RESPONSE))
    )

  def unknownError(status: Int, correlationId: Option[String]): StatusCheckErrorResponseWithStatus =
    StatusCheckErrorResponseWithStatus(status, StatusCheckErrorResponse(correlationId, StatusCheckError(ERR_UNKNOWN)))

}
