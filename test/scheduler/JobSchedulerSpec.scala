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

import scala.concurrent.ExecutionContext

class JobSchedulerSpec extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach with Eventually {
  given ExecutionContext = ExecutionContext.global

  private val schedulerFactory: SchedulerFactory  = new StdSchedulerFactory()
  private val mockLifecycle: ApplicationLifecycle = mock[ApplicationLifecycle]
  private val mockJobFactory: ScheduledJobFactory = mock[ScheduledJobFactory]
  private val mockAppConfig: AppConfig            = mock[AppConfig]
  private lazy val jobScheduler = new JobScheduler(mockLifecycle, schedulerFactory, mockJobFactory, mockAppConfig)

  override def beforeEach(): Unit = {
    when(mockAppConfig.logCertificateExpirySchedule).thenReturn("0 0 4 * * ? 2099")
    when(mockAppConfig.isCertificateExpirySchedulePresent).thenReturn(true)
    when(mockJobFactory.newJob(any(), any())).thenReturn(_ => Thread.sleep(50))
    ()
  }

  "JobScheduler.logCertificateExpiryStatus" should "return trigger status NORMAL when the job is not running" in {
    jobScheduler.logCertificateExpiryStatus() mustBe JobStatus(TriggerState.NORMAL)
  }

  it should "return the status BLOCKED when the job is running" in {
    jobScheduler.startLogCertificateExpiry()
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
