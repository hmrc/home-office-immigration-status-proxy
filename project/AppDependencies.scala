import sbt.*
import sbt.librarymanagement.InclExclRule

object AppDependencies {

  private val bootstrapPlayVersion = "10.7.1"
  val quartzVersion                = "2.5.0"
  val playSuffix                   = "-play-30"
  val hmrcMongoVersion             = "2.12.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30"    % bootstrapPlayVersion,
    "uk.gov.hmrc"                  %% "domain-play-30"               % "13.0.0",
    "uk.gov.hmrc"                  %% "internal-auth-client-play-30" % "4.4.0",
    "org.typelevel"                %% "cats-core"                    % "2.13.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"         % "2.20.0",
    ("org.quartz-scheduler"         % "quartz"                       % quartzVersion).withExclusions(
      Vector(
        InclExclRule().withOrganization("com.mchange").withName("c3p0"),
        InclExclRule().withOrganization("com.mchange").withName("mchange-commons-java")
      )
    ),
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playSuffix" % hmrcMongoVersion
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
