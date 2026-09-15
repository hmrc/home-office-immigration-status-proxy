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

import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext}
import play.api.Logging
import util.CertificatesCheck

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import javax.inject.Inject

@DisallowConcurrentExecution
class LogCertificateExpiryJob @Inject (certificatesCheck: CertificatesCheck) extends Job with Logging {

  private val jobName = "log-certificate-expiry"

  private def executeJob(): Unit = {
    logger.info("RUNNING certificate expiry job")
    certificatesCheck.getCertificateDetails match {
      case Some(cd) =>
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy ' at 'HH:mm")
        val nowPlus90Days                    = LocalDateTime.now().plus(90, DAYS)
        if (cd.date.isBefore(nowPlus90Days)) {
          logger.warn(
            s"Certificate issued by ${cd.issuerName} with subject ${cd.subject} expires in less than 90 days on ${cd.date
                .format(dateFormatter)}"
          )
        } else {
          logger.info(
            s"Certificate issued by ${cd.issuerName} with subject ${cd.subject} expires on ${cd.date.format(dateFormatter)}"
          )
        }
      case _ =>
        logger.warn("No certificate found")
        ()
    }
  }

  override def execute(context: JobExecutionContext): Unit = executeJob()

}
