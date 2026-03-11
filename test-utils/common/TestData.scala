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

package common

import models.OAuthToken

trait TestData {
  protected val validTokenForm = "grant_type=client_credentials&client_id=hmrc&client_secret=TBC"

  protected val tokenUrl = "/v1/status/public-funds/token"

  protected val ninoUrl            = "/v1/status/public-funds/nino"
  protected val ninoUrlWithService = "/v1/status/public-funds/nino/service-a"
  protected val mrzUrl             = "/v1/status/public-funds/mrz"

  protected val tokenId                = "FOO0123456789"
  protected val tokenType              = "SomeTokenType"
  protected val oAuthToken: OAuthToken = OAuthToken(tokenId, tokenType)

  protected val bearerId      = "Bearer123"
  protected val correlationId = "some-correlation-id"

  protected val dob                     = "2001-01-31"
  protected val familyName              = "Doe"
  protected val givenName               = "Jane"
  protected val fullName                = s"$givenName $familyName"
  protected val nino                    = "RJ301829A"
  protected val documentNumber          = "1234567890"
  protected val documentType            = "PASSPORT"
  protected val nationality             = "USA"
  protected val rangeStartDate          = "2019-04-15"
  protected val rangeEndDate            = "2019-07-15"
  protected val productType             = "EUS"
  protected val immigrationStatus       = "ILR"
  protected val noRecourseToPublicFunds = true
  protected val statusEndDate           = "2018-01-31"
  protected val statusStartDate         = "2018-01-31"
}
