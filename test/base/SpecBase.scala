/*
 * Copyright 2026 HM Revenue & Customs
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

package base

import common.TestData
import org.scalatest.Inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration.*

trait SpecBase extends AnyWordSpecLike with Matchers with Inside with GuiceOneAppPerSuite with TestData {

  val timeoutDuration: FiniteDuration   = 5.seconds
  def await[T](future: Awaitable[T]): T = Await.result(future, timeoutDuration)
}
