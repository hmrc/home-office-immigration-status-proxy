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

import org.quartz.Trigger.TriggerState
import org.scalatest.Inspectors
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class JobStatusSpec extends AnyWordSpec with Matchers with Inspectors {
  private val triggerStates = TriggerState
    .values()
    .filterNot(Set(TriggerState.NORMAL, TriggerState.BLOCKED))

  "JobStatus" should {
    "serialize TriggerState.NORMAL as IDLE" in {
      Json.toJson(JobStatus(TriggerState.NORMAL)) shouldBe Json.obj("status" -> "IDLE")
    }

    "serialize TriggerState.BLOCKED as RUNNING" in {
      Json.toJson(JobStatus(TriggerState.BLOCKED)) shouldBe Json.obj("status" -> "RUNNING")
    }

    "serialize other statuses as their names" in forEvery(triggerStates) { state =>
      Json.toJson(JobStatus(state)) shouldBe Json.obj("status" -> state.toString)
    }
  }
}
