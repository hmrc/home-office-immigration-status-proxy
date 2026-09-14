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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.lock.{LockService, MongoLockRepository}
import uk.gov.hmrc.mongo.transaction.{TransactionConfiguration, Transactions}

import javax.inject.Inject
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}

@DisallowConcurrentExecution
class LogCertificateExpiryJob @Inject() (
  val mongoComponent: MongoComponent,
  val lockRepository: MongoLockRepository
)(using ec: ExecutionContext)
    extends Job
    with LockService
    with Logging
    with Transactions {

  private val jobName = "log-certificate-expiry"

  given HeaderCarrier = HeaderCarrier()

  given TransactionConfiguration = TransactionConfiguration.strict

  override val lockId: String = jobName
  override val ttl: Duration  = 1.hour

  private def executeJob(): Future[Unit] = {
    logger.warn("\nRUNNING certificate expiry job")
    Future.successful((): Unit)
  }

  override def execute(context: JobExecutionContext): Unit =
    Await.result(
      withLock(executeJob()).map {
        _.getOrElse {
          logger.info(s"$jobName job lock could not be obtained")
        }
      },
      Duration.Inf
    )
}
