import play.sbt.PlayImport.ws
import sbt.{CrossVersion, ModuleID, compilerPlugin}
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.1"

  private val compile = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28"      % "5.8.0",
    "com.kenshoo"         %% "metrics-play"                   % "2.7.3_0.8.1",
    "uk.gov.hmrc"         %% "domain"                         % "6.2.0-play-28",
    "com.github.blemale"  %% "scaffeine"                      % "5.1.0",
    "uk.gov.hmrc"         %% "agent-kenshoo-monitoring"       % "4.7.0-play-27",
    "org.typelevel"       %% "cats-core"                      % "2.0.0",
    ws
  )

  private val test = Seq(
    "org.scalatest"             %% "scalatest"              % "3.2.9",
    "org.mockito"               % "mockito-core"            % "3.11.2",
    "org.pegdown"               % "pegdown"                 % "1.6.0" ,
    "org.scalatestplus.play"    %% "scalatestplus-play"     % "5.1.0",
    "org.scalatestplus"         %% "mockito-3-4"            % "3.2.9.0",
    "com.github.tomakehurst"    % "wiremock"                % "2.27.2",
    "com.vladsch.flexmark"      % "flexmark-all"            % "0.35.10"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val jettyVersion = "9.2.24.v20180105"

  val jettyOverrides = Seq(
    "org.eclipse.jetty" % "jetty-server" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-security" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-xml" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-client" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-http" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-io" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty" % "jetty-util" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % IntegrationTest,
    "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
