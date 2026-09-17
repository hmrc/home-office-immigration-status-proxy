/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import wiring.AppConfig

import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.DAYS
import javax.security.auth.x500.X500Principal
import scala.concurrent.ExecutionContext
import scala.util.Success

class CertificatesCheckSpec
    extends PlaySpec
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with LogCapturing {

  given ExecutionContext = ExecutionContext.global

  private val testPrincipalIssuerName  = "test principal issuer"
  private val testPrincipalSubjectName = "test principal subject"

  private val testLogger: Logger = Logger("test-logger")

  private val mockAppConfig = mock[AppConfig]

  private val certificatesCheck: CertificatesCheck = new CertificatesCheck(mockAppConfig) {
    override protected val logger: Logger = testLogger
  }

  import java.time.{Instant, LocalDate, ZoneId}
  import java.util.Date

  val testDate: Date = Date.from(Instant.now().plus(30, DAYS))
  val testDateLocalDate: LocalDateTime = {
    def toLocalDateTime(date: Date): LocalDateTime =
      Instant.ofEpochMilli(date.getTime).atZone(ZoneId.of("Europe/London")).toLocalDateTime
    toLocalDateTime(testDate)
  }

  override def beforeEach(): Unit = {
    reset(
      mockAppConfig
    )

    ()
  }

  "LogCertificateExpiryJob.logCertificateExpiry" must {
    "get None when there is no certificate path or password" in {
      when(mockAppConfig.privateCertificatePath).thenReturn(None)
      when(mockAppConfig.privateCertificatePassword).thenReturn(None)
      certificatesCheck.getCertificateDetails mustBe None
    }

    "get None when the certificate path is invalid" in {
      when(mockAppConfig.privateCertificatePath).thenReturn(Some("/bla"))
      when(mockAppConfig.privateCertificatePassword).thenReturn(Some("password"))
      withCaptureOfLoggingFrom(testLogger) { logs =>
        certificatesCheck.getCertificateDetails mustBe None
        logs.map(l => l.getLevel.levelStr + ":" + l.getFormattedMessage) mustBe Seq("WARN:Unable to load certificate")
        ()
      }
    }

    "get certificate when the certificate path is valid" in {
      when(mockAppConfig.privateCertificatePath).thenReturn(Some("/bla"))
      when(mockAppConfig.privateCertificatePassword).thenReturn(Some("password"))

      val spyCertificatesCheck = spy(certificatesCheck)
      val mockX509Certificate  = mock[X509Certificate]
      when(spyCertificatesCheck.tryRetrieveCertificate(any(), any())).thenReturn(Success(mockX509Certificate))
      when(mockX509Certificate.getNotAfter).thenReturn(testDate)
      val mockX500PrincipalIssuer = mock[X500Principal]
      when(mockX500PrincipalIssuer.getName).thenReturn(testPrincipalIssuerName)
      val mockX500PrincipalSubject = mock[X500Principal]
      when(mockX500PrincipalSubject.getName).thenReturn(testPrincipalSubjectName)
      when(mockX509Certificate.getIssuerX500Principal).thenReturn(mockX500PrincipalIssuer)
      when(mockX509Certificate.getSubjectX500Principal).thenReturn(mockX500PrincipalSubject)

      withCaptureOfLoggingFrom(testLogger) { logs =>
        spyCertificatesCheck.getCertificateDetails.map(_.subject) mustBe Some(testPrincipalSubjectName)
        spyCertificatesCheck.getCertificateDetails.map(_.issuerName) mustBe Some(testPrincipalIssuerName)
        spyCertificatesCheck.getCertificateDetails.map(_.date) mustBe Some(testDateLocalDate)
        logs.length mustBe 0
        ()
      }
    }
  }

}
