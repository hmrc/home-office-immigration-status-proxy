import play.sbt.PlayImport.ws
import sbt.{CrossVersion, ModuleID, compilerPlugin}
import sbt._

object AppDependencies {

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.3.0",
    "com.kenshoo" %% "metrics-play" % "2.7.3_0.8.1",
    "uk.gov.hmrc" %% "domain" % "5.11.0-play-27",
    "com.github.blemale" %% "scaffeine" % "3.1.0",
    "uk.gov.hmrc" %% "agent-kenshoo-monitoring" % "4.5.0-play-27",
    ws
  )

  private val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.9",
    "org.mockito" % "mockito-core" % "3.1.0",
    "org.pegdown" % "pegdown" % "1.6.0" ,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3",
    "com.github.tomakehurst" % "wiremock" % "2.27.1"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.0" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.0" % Provided cross CrossVersion.full
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
