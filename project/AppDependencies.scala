import sbt.*

import scala.collection.immutable.Seq

object AppDependencies {

  private val bootstrapPlayVersion = "10.4.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30"    % bootstrapPlayVersion,
    "uk.gov.hmrc"                  %% "domain-play-30"               % "13.0.0",
    "uk.gov.hmrc"                  %% "internal-auth-client-play-30" % "4.3.0",
    "org.typelevel"                %% "cats-core"                    % "2.13.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"         % "2.20.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
