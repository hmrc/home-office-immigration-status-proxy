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

package uk.gov.hmrc.homeofficesettledstatusproxy.wiring

import java.net.URL

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {

  val rightToPublicFundsPathPrefix: String
  val rightToPublicFundsBaseUrl: URL

  val homeOfficeClientId: String
  val homeOfficeClientSecret: String

  val authBaseUrl: String

}

class AppConfigImpl @Inject()(config: ServicesConfig) extends AppConfig {

  val rightToPublicFundsPathPrefix: String =
    config.getConfString("home-office-right-to-public-funds.pathPrefix", "")

  val rightToPublicFundsBaseUrl: URL =
    new URL(config.baseUrl("home-office-right-to-public-funds"))

  val homeOfficeClientId: String =
    config.getConfString("home-office-right-to-public-funds.client_id", "")
  val homeOfficeClientSecret: String =
    config.getConfString("home-office-right-to-public-funds.client_secret", "")

  val authBaseUrl: String = config.baseUrl("auth")

}
