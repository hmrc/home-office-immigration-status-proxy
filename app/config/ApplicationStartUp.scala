/*
 * Copyright 2023 HM Revenue & Customs
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

package config

import play.api.Logging
import util.CertificatesCheck
import wiring.AppConfig

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationStartUp @Inject() (config: AppConfig, certificatesCheck: CertificatesCheck)(implicit
  ec: ExecutionContext
) extends Logging {
  if (config.logCertificateExpiryOnStartup) {
    certificatesCheck.getCertificateDetails match {
      case Some(cd) =>
        if (cd.date.before(Date.from(Instant.now().plus(60, DAYS)))) {
          logger.error(
            s"privateCertificate issued by ${cd.issuerName} with subject ${cd.subject} expires in less than 60 days on ${cd.date}"
          )
        } else {
          logger.warn(s"privateCertificate issued by ${cd.issuerName} with subject ${cd.subject} expires on ${cd.date}")
        }
      case _ =>
        logger.warn("No certificate details found")
    }
  } else {
    ()
  }

}
