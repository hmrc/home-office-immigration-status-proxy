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

package util

import cats.implicits.*
import play.api.Logging
import wiring.AppConfig

import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.{Certificate, X509Certificate}
import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

@Singleton
class CertificatesCheck @Inject() (config: AppConfig)(implicit ec: ExecutionContext) extends Logging {
  case class CertificateDetails(date: Date, issuerName: String, subject: String)

  private def tryRetrieveCertificate(certificatePath: String, password: Array[Char]): Try[Certificate] =
    Using(new FileInputStream(certificatePath)) { fis =>
      val keyStore = KeyStore.getInstance("PKCS12")
      keyStore.load(fis, password)
      keyStore
        .aliases()
        .asScala
        .map(alias => keyStore.getCertificate(alias))
        .toList
        .head
    }

  def getCertificateDetails: Option[CertificateDetails] =
    (config.privateCertificatePath, config.privateCertificatePassword.map(_.toCharArray)).flatMapN {
      case (certificatePath, password) =>
        tryRetrieveCertificate(certificatePath, password).fold[Option[CertificateDetails]](
          { ex =>
            logger.warn("Unable to load certificate", ex)
            None
          },
          {
            case certificate: X509Certificate =>
              Some(
                CertificateDetails(
                  certificate.getNotAfter,
                  certificate.getIssuerX500Principal.getName,
                  certificate.getSubjectX500Principal.getName
                )
              )
            case cert: Certificate =>
              logger.warn(s"Wrong type of cert, cert was of type: ${cert.getType}")
              None
          }
        )
    }
}
