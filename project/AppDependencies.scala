import play.sbt.PlayImport.ws
import sbt.{CrossVersion, ModuleID, compilerPlugin}
import sbt._

object AppDependencies {

  private val silencerVersion = "1.7.9"
  private val bootstrapPlayVersion = "5.24.0"

  private val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28"  % bootstrapPlayVersion,
    "com.kenshoo"                  %% "metrics-play"               % "2.7.3_0.8.2",
    "uk.gov.hmrc"                  %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc"                  %% "agent-kenshoo-monitoring"   % "4.8.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.8.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.13.3",
    ws
  )

  private val test = Seq(
    "org.scalatest"             %% "scalatest"               % "3.2.9",
    "uk.gov.hmrc"               %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "org.scalatestplus"         %% "mockito-4-5"             % "3.2.12.0",
    "com.github.tomakehurst"    % "wiremock-jre8"            % "2.33.2",
    "com.vladsch.flexmark"      % "flexmark-all"             % "0.62.2"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
