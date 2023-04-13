import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion = "7.12.0"

  private val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28"     % bootstrapPlayVersion,
    "com.kenshoo"                  %% "metrics-play"                  % "2.7.3_0.8.2",
    "uk.gov.hmrc"                  %% "domain"                        % "8.1.0-play-28",
    "uk.gov.hmrc"                  %% "internal-auth-client-play-28"  % "1.4.0",
    "org.typelevel"                %% "cats-core"                     % "2.9.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"          % "2.14.1",
    ws
  )

  private val test = Seq(
    "org.scalatest"         %% "scalatest"               % "3.2.15",
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "org.mockito"           %% "mockito-scala-scalatest" % "1.17.12",
    "com.github.tomakehurst" % "wiremock-jre8"           % "2.35.0",
    "com.vladsch.flexmark"   % "flexmark-all"            % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
