import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.5.1"
ThisBuild / majorVersion := 0

val commonSettings: Seq[String] = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:noAutoTupling",
  "-Wvalue-discard",
  "-Werror",
  "-Wconf:src=routes/.*:s",
  "-Wconf:src=views/.*txt.*:s",
  "-Wconf:msg=unused import&src=conf/.*:s",
  "-Wconf:msg=unused import&src=views/.*:s",
  "-Wconf:msg=Flag.*repeatedly:s",
  "-Wunused:unsafe-warn-patvars",
  "-Wunused:nowarn"
)

lazy val microservice = (project in file("."))
  .settings(
    name := "home-office-immigration-status-proxy",
    PlayKeys.playDefaultPort := 10211,
    libraryDependencies ++= AppDependencies(),
    scalacOptions := commonSettings
  )
  .settings(CodeCoverageSettings())
  .enablePlugins(PlayScala, SbtDistributablesPlugin, BuildInfoPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings(), scalacOptions := commonSettings)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
