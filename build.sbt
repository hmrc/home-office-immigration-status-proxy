import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.DefaultBuildSettings

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt IntegrationTest/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle IntegrationTest/scalastyle")

lazy val root = (project in file("."))
  .settings(
    name := "home-office-immigration-status-proxy",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.10",
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always),
    PlayKeys.playDefaultPort := 10211,
    libraryDependencies ++= AppDependencies(),
    publishingSettings,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings: _*)
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := DefaultBuildSettings.oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtDistributablesPlugin)

scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s"
)
