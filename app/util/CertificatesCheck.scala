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

import java.io.ByteArrayInputStream
import java.security.cert.X509Certificate
import java.security.{KeyStore, PrivateKey}
import java.util.{Base64, Date}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}
@Singleton
class CertificatesCheck @Inject() (config: AppConfig)(implicit ec: ExecutionContext) extends Logging {
  case class CertificateDetails(date: Date, issuerName: String, subject: String)

  def getCertificateDetailsMethod2: Seq[CertificateDetails] = {

//    val fis = new FileInputStream("play.ws.ssl.keyManager.stores.0.path")
//    try {
//      ks.load(fis, "play.ws.ssl.keyManager.stores.0.password")
//    } finally {
//      fis.close()
//    }
    val keyStore   = KeyStore.getInstance("PKCS12")
    val aliasNames = keyStore.aliases().asScala.toSeq
    // Need to load keystore here
    aliasNames.flatMap { alias =>
      keyStore.getCertificate(alias) match {
        case cert: X509Certificate =>
          Seq(
            CertificateDetails(
              cert.getNotAfter,
              cert.getIssuerX500Principal.getName,
              cert.getSubjectX500Principal.getName
            )
          )
        case _ => Nil
      }
    }
  }

  def getCertificateDetails: Option[CertificateDetails] = {
    logger.warn("*** private certificate: " + config.privateCertificate)
    (config.privateCertificate, config.privateCertificatePassword).flatMapN { case (certificate, password) =>
      val keyStore = KeyStore.getInstance("PKCS12")
      val aliasNames = keyStore.aliases().asScala.toSeq

      val decodedPrivateCertificate = Base64.getDecoder.decode(certificate)
      keyStore.load(new ByteArrayInputStream(decodedPrivateCertificate), password.toCharArray)
      logger.warn("*** aliasNames: " + aliasNames)
      aliasNames
        .map(isPrivateX509(keyStore, password))
        .find(_.isSuccess)
        .getOrElse(Failure(new IllegalStateException("No valid key-certificate pair in the key store"))) match {
        case Success((_, cert)) =>
          val c = CertificateDetails(
            cert.getNotAfter,
            cert.getIssuerX500Principal.getName,
            cert.getSubjectX500Principal.getName
          )
          logger.warn(s"Certificate details found: $c")
          Some(c)
        case Failure(ex) =>
          logger.warn("Exception when retrieving certificate", ex)
          None
      }
    }
  }

  private def isPrivateX509(keyStore: KeyStore, password: String)(alias: String) =
    for {
      key  <- Try(keyStore.getKey(alias, password.toCharArray).asInstanceOf[PrivateKey])
      cert <- Try(keyStore.getCertificate(alias).asInstanceOf[X509Certificate])
    } yield (key, cert)
}
