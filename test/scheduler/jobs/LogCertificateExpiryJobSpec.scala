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

package scheduler.jobs

import org.apache.pekko.actor.ActorSystem
import org.mockito.Mockito.*
import org.quartz.JobExecutionContext
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Logger
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import util.{CertificateDetails, CertificatesCheck}
import wiring.AppConfig

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

class LogCertificateExpiryJobSpec
    extends PlaySpec
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with LogCapturing {

  val testLogger: Logger = Logger("test-logger")

  private val appConfig               = mock[AppConfig]
  private val mockCertificatesCheck   = mock[CertificatesCheck]
  private val mockJobExecutionContext = mock[JobExecutionContext]

  given ActorSystem      = ActorSystem("test")
  given ExecutionContext = ExecutionContext.global

  private val (testDateCritical, testDateNonCritical) = {
    val now              = LocalDate.now()
    val justBeforeExpiry = now.plusDays(89L)
    val justAfterExpiry  = now.plusDays(90L)
    (justBeforeExpiry, justAfterExpiry)
  }

  private val certificateDetailsCritical: CertificateDetails =
    CertificateDetails(testDateCritical, "issuerName", "subject")
  private val certificateDetailsNonCritical: CertificateDetails =
    CertificateDetails(testDateNonCritical, "issuerName", "subject")
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  private val certificateExpiryJob: LogCertificateExpiryJob = new LogCertificateExpiryJob(mockCertificatesCheck) {
    override protected val logger: Logger = testLogger
  }
  override def beforeEach(): Unit = {
    reset(
      appConfig,
      mockCertificatesCheck
    )
    ()
  }

  "LogCertificateExpiryJob.logCertificateExpiry" must {
    "get the certificate details and log a warning that job running when no certificate present" in {
      when(mockCertificatesCheck.getCertificateDetails).thenReturn(None)
      withCaptureOfLoggingFrom(testLogger) { logs =>
        certificateExpiryJob.execute(mockJobExecutionContext)
        verify(mockCertificatesCheck, times(1)).getCertificateDetails
        logs.count(_.getLevel == ch.qos.logback.classic.Level.WARN) mustBe 1
        logs.headOption.map(_.getFormattedMessage) mustBe Some("RUNNING certificate expiry job")
        ()
      }
    }
    "get the certificate details and log a warning that job running and a second with certificate info " +
      "when a certificate present and expiring in less than 90 days" in {
        when(mockCertificatesCheck.getCertificateDetails).thenReturn(Some(certificateDetailsCritical))
        withCaptureOfLoggingFrom(testLogger) { logs =>
          certificateExpiryJob.execute(mockJobExecutionContext)
          verify(mockCertificatesCheck, times(1)).getCertificateDetails
          logs.count(_.getLevel == ch.qos.logback.classic.Level.WARN) mustBe 2
          logs.map(_.getFormattedMessage) mustBe Seq(
            "RUNNING certificate expiry job",
            s"Certificate issued by issuerName with subject subject expires in less than 90 days on ${testDateCritical.format(dateFormatter)}"
          )
          ()
        }
      }
    "get the certificate details and log a warning that job running and a second with certificate info " +
      "when a certificate present and expiring in >= 90 days" in {
        when(mockCertificatesCheck.getCertificateDetails).thenReturn(Some(certificateDetailsNonCritical))
        withCaptureOfLoggingFrom(testLogger) { logs =>
          certificateExpiryJob.execute(mockJobExecutionContext)
          verify(mockCertificatesCheck, times(1)).getCertificateDetails
          logs.count(_.getLevel == ch.qos.logback.classic.Level.INFO) mustBe 1
          logs.count(_.getLevel == ch.qos.logback.classic.Level.WARN) mustBe 1
          logs.map(_.getFormattedMessage) mustBe Seq(
            "RUNNING certificate expiry job",
            s"Certificate issued by issuerName with subject subject expires on ${testDateNonCritical.format(dateFormatter)}"
          )
          ()
        }
      }
  }

}
