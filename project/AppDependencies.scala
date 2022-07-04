import play.sbt.PlayImport.ws
import sbt.{CrossVersion, ModuleID, compilerPlugin}
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.9"

  private val compile = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28"      % "5.24.0",
    "com.kenshoo"         %% "metrics-play"                   % "2.7.3_0.8.1",
    "uk.gov.hmrc"         %% "domain"                         % "7.0.0-play-28",
    "uk.gov.hmrc"         %% "agent-kenshoo-monitoring"       % "4.8.0-play-28",
    "org.typelevel"       %% "cats-core"                      % "2.0.0",
    ws
  )

  private val test = Seq(
    "org.scalatest"             %% "scalatest"               % "3.2.9",
    "org.mockito"               % "mockito-core"             % "3.11.2",
    "org.pegdown"               % "pegdown"                  % "1.6.0" ,
    "org.scalatestplus.play"    %% "scalatestplus-play"      % "5.1.0",
    "org.scalatestplus"         %% "mockito-3-4"             % "3.2.9.0",
    "com.github.tomakehurst"    % "wiremock-jre8"            % "2.27.2",
    "com.vladsch.flexmark"      % "flexmark-all"             % "0.35.10"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
