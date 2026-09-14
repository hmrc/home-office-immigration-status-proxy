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

package scheduler

import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{CronScheduleBuilder, Scheduler, SchedulerFactory, Trigger}
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import scheduler.jobs.LogCertificateExpiryJob
import wiring.AppConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JobScheduler @Inject() (
  lifecycle: ApplicationLifecycle,
  schedulerFactory: SchedulerFactory,
  jobFactory: ScheduledJobFactory,
  appConfig: AppConfig
)(using
  ec: ExecutionContext
) extends Logging {
  private lazy val quartz: Scheduler = schedulerFactory.getScheduler

  private lazy val logCertificateExpiryJobDetail = newJob(classOf[LogCertificateExpiryJob])
    .withIdentity("log-certificate-expiry")
    .build()

  private lazy val logCertificatExpiryJobSchedule = CronScheduleBuilder
    .cronSchedule(appConfig.logCertificateExpirySchedule)

  private lazy val logCertificateExpiryJobTrigger = newTrigger()
    .forJob(logCertificateExpiryJobDetail)
    .withSchedule(logCertificatExpiryJobSchedule)
    .build()

  private def getJobStatus(trigger: Trigger): JobStatus = JobStatus(quartz.getTriggerState(trigger.getKey))

  def startLogCertficateExpiry(): Unit = quartz.triggerJob(logCertificateExpiryJobDetail.getKey)

  def logCertificateExpiryStatus(): JobStatus = getJobStatus(logCertificateExpiryJobTrigger)

  private def startScheduler(): Unit = {
    val quartz = StdSchedulerFactory.getDefaultScheduler

    quartz.setJobFactory(jobFactory)

    lifecycle.addStopHook(() => Future(quartz.shutdown()))

    quartz.scheduleJob(logCertificateExpiryJobDetail, logCertificateExpiryJobTrigger)
    quartz.start()
  }

  if (appConfig.isCertificateExpirySchedulePresent) {
    logger.warn("Certificate expiry schedule present")
    startScheduler()
  } else {
    logger.warn("No certificate expiry schedule present")
  }
}
