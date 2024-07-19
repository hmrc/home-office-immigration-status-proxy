import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.0.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30"    % bootstrapPlayVersion,
    "uk.gov.hmrc"                  %% "domain-play-30"               % "10.0.0",
    "uk.gov.hmrc"                  %% "internal-auth-client-play-30" % "3.0.0",
    "org.typelevel"                %% "cats-core"                    % "2.12.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"         % "2.17.2"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
