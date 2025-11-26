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

package wiring

import com.google.inject.{ImplementedBy, Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (config: ServicesConfig) {

  val rightToPublicFundsBaseUrl: String = config.baseUrl("home-office-right-to-public-funds")

  val tokenURL: String = config.getConfString("home-office-right-to-public-funds.url.token", "")
  val ninoURL: String  = config.getConfString("home-office-right-to-public-funds.url.nino", "")
  val mrzURL: String   = config.getConfString("home-office-right-to-public-funds.url.mrz", "")

  val homeOfficeClientId: String =
    config.getConfString("home-office-right-to-public-funds.client_id", "")
  val homeOfficeClientSecret: String =
    config.getConfString("home-office-right-to-public-funds.client_secret", "")

  val authBaseUrl: String = config.baseUrl("auth")

}
