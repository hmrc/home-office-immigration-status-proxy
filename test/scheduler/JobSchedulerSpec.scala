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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.quartz.Trigger.TriggerState
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{Job, SchedulerFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.ApplicationLifecycle
import wiring.AppConfig

import scala.compiletime.uninitialized
import scala.concurrent.ExecutionContext

class JobSchedulerSpec extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach with Eventually {
  given ExecutionContext = ExecutionContext.global

  private val schedulerFactory: SchedulerFactory = new StdSchedulerFactory()
  private var lifecycle: ApplicationLifecycle    = uninitialized
  private var jobFactory: ScheduledJobFactory    = uninitialized
  private var appConfig: AppConfig               = uninitialized
  private var jobScheduler: JobScheduler         = uninitialized

  override def beforeEach(): Unit = {
    // Clear down any job or schedule information
    schedulerFactory.getScheduler.clear()

    lifecycle = mock[ApplicationLifecycle]
    jobFactory = mock[ScheduledJobFactory]
    appConfig = mock[AppConfig]

    when(appConfig.logCertificateExpirySchedule).thenReturn("0 0 4 * * ? 2099")
    when(jobFactory.newJob(any(), any())).thenReturn(_ => Thread.sleep(50))

    jobScheduler = new JobScheduler(lifecycle, schedulerFactory, jobFactory, appConfig)
  }

  "JobScheduler.logCertificateExpiryStatus" should "return trigger status NORMAL when the job is not running" in {
    jobScheduler.logCertificateExpiryStatus() mustBe JobStatus(TriggerState.NORMAL)
  }

  it should "return the status BLOCKED when the job is running" in {
    jobScheduler.startLogCertficateExpiry()
    // Trigger state should go to blocked while the job is running
    eventually {
      jobScheduler.logCertificateExpiryStatus() mustBe JobStatus(TriggerState.BLOCKED)
    }
    // It should return to normal once the job ends
    eventually {
      jobScheduler.logCertificateExpiryStatus() mustBe JobStatus(TriggerState.NORMAL)
    }
  }
}
