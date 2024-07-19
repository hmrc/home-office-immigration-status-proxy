import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "2.13.14"
ThisBuild / majorVersion := 0

lazy val microservice = (project in file("."))
  .settings(
    name := "home-office-immigration-status-proxy",
    PlayKeys.playDefaultPort := 10211,
    libraryDependencies ++= AppDependencies(),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(CodeCoverageSettings.settings)
  .enablePlugins(PlayScala, SbtDistributablesPlugin, BuildInfoPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
