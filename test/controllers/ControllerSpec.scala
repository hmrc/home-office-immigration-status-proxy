/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import org.mockito.ArgumentMatchers.any
import akka.util.Timeout
import wiring.AppConfig
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import connectors.HomeOfficeRightToPublicFundsConnector
import play.api.mvc.Result

import scala.concurrent.Future
import play.mvc.Http.HeaderNames
import play.api.http.MimeTypes
import models.{OAuthToken, StatusCheckErrorResponseWithStatus, StatusCheckResponse}
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}
import scala.language.postfixOps

trait ControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to[FakeAuthAction],
      bind[HomeOfficeRightToPublicFundsConnector].toInstance(mockConnector)
    )
    .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val mockConnector                     = mock(classOf[HomeOfficeRightToPublicFundsConnector])
  val timeoutDuration: FiniteDuration   = 5 seconds
  implicit val timeout: Timeout         = Timeout(timeoutDuration)
  def await[T](future: Awaitable[T]): T = Await.result(future, timeoutDuration)
  lazy val messages: Messages           = inject[MessagesApi].preferred(Seq.empty)
  lazy val appConfig: AppConfig         = inject[AppConfig]
  val correlationId                     = "CorrelationId123"

  def tokenCallFails: OngoingStubbing[Future[OAuthToken]] =
    when(mockConnector.token(any(), any())(any()))
      .thenReturn(Future.failed(new Exception("Oh no - token")))
  def tokenCallIsSuccessful: OngoingStubbing[Future[OAuthToken]] =
    when(mockConnector.token(any(), any())(any()))
      .thenReturn(Future.successful(OAuthToken("String", "String")))
  def requestMrzCallFails: OngoingStubbing[Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]] =
    when(mockConnector.statusPublicFundsByMrz(any(), any(), any(), any())(any()))
      .thenReturn(Future.failed(new Exception("Oh no - connector")))
  def requestMrzCallIsSuccessful(
    response: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]
  ): OngoingStubbing[Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]] =
    when(mockConnector.statusPublicFundsByMrz(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(response))
  def requestNinoCallFails: OngoingStubbing[Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]] =
    when(mockConnector.statusPublicFundsByNino(any(), any(), any(), any())(any()))
      .thenReturn(Future.failed(new Exception("Oh no - connector")))
  def requestNinoCallIsSuccessful(
    response: Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]
  ): OngoingStubbing[Future[Either[StatusCheckErrorResponseWithStatus, StatusCheckResponse]]] =
    when(mockConnector.statusPublicFundsByNino(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful(response))

  def withHeaders(result: Result): Result =
    result
      .withHeaders("X-Correlation-Id" -> correlationId, HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
}
