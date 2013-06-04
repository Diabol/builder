import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "builder-app"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      javaCore,
      javaEbean,
      "org.mockito" % "mockito-core" % "1.9.0" % "test"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
