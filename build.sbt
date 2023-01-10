import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimumStmtTotal := 90.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt IntegrationTest/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle IntegrationTest/scalastyle")

lazy val root = (project in file("."))
  .settings(
    name := "home-office-immigration-status-proxy",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.13.10",
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always),
    PlayKeys.playDefaultPort := 10211,
    libraryDependencies ++= AppDependencies(),
    publishingSettings,
    scoverageSettings,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtDistributablesPlugin)

scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s"
)

Global / excludeLintKeys += IntegrationTest / scalafmtOnCompile

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests.map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
