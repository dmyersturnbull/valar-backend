import com.typesafe.config.{ConfigException, ConfigFactory, Config}


name := "valar"

description := "Kokel Lab all-knowing relational database"

lazy val commonSettings = Seq(
	organization := "com.github.dmyersturnbull",
	organizationHomepage := Some(url("https://github.com/dmyersturnbull")),
	version := "1.0.0-SNAPSHOT",
	isSnapshot := true,
	scalaVersion := "2.12.6",
	javacOptions ++= Seq("-source", "10", "-target", "10", "-Xlint:all"),
	javaOptions += "-Xmx1G",
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps"),
	testOptions in Test += Tests.Argument("-oF"),
	homepage := Some(url("https://github.com/dmyersturnbull/valar-infrastructure")),
	developers := List(Developer("dmyersturnbull", "Douglas Myers-Turnbull", "dmyersturnbull@dmyersturnbull.com", url("https://github.com/dmyersturnbull"))),
	startYear := Some(2016),
	scmInfo := Some(ScmInfo(url("https://github.com/dmyersturnbull/valar-infrastructure"), "https://github.com/dmyersturnbull/valar-infrastructure.git")),
	libraryDependencies ++= Seq(
		"com.typesafe" % "config" % "1.3.0",
		"com.iheart" %% "ficus" % "1.4.0",
		"com.typesafe.slick" %% "slick" % "3.2.1",
		"com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
		"com.typesafe.slick" %% "slick-codegen" % "3.2.1",
		"org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
		"mysql" % "mysql-connector-java" % "6.0.6",
		"com.jsuereth" %% "scala-arm" % "2.0",
		"com.github.scopt" %% "scopt" % "3.5.0",
		"io.argonaut" %% "argonaut" % "6.2",
		"de.svenkubiak" % "jBCrypt" % "0.4.1",
		"com.google.guava" % "guava" % "25.1-jre",
		"com.google.code.findbugs" % "jsr305" % "3.0.1", // to work around compiler warnings about missing anotations from Guava
		"org.slf4j" % "slf4j-api" % "1.8.0-beta2",
		"com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
		"org.typelevel"  %% "squants"  % "1.3.0",
		"org.scalatest" %% "scalatest" % "3.0.5" % "test",
		"org.scalactic" %% "scalactic" % "3.0.5" % "test",
		"org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
		"org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
	) map (_.exclude("org.slf4j", "slf4j-log4j12")),
	dependencyOverrides += "com.google.guava" % "guava" % "25.1-jre", // some dependency uses 17.0
	dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.8.0-beta2" // can't use 1.7.x is incompatible
)

libraryDependencies ++= Seq(
	"com.typesafe.slick" %% "slick-codegen" % "3.2.1"
)

lazy val core = project.
		settings(commonSettings: _*)

lazy val params = project.
	settings(commonSettings: _*).
	dependsOn(core)

lazy val insertion = project.
	settings(commonSettings: _*).
	dependsOn(core).
	dependsOn(params)

lazy val importer = project.
	settings(commonSettings: _*).
	dependsOn(core).
	dependsOn(params).
	dependsOn(insertion)

lazy val root = (project in file(".")).
		settings(commonSettings: _*).
		aggregate(core, params, insertion, importer)

val slickGen = taskKey[Unit]("Generates slick tables")

slickGen := {
	val conf = ConfigFactory.parseFile(new File("conf/application.conf"))
	slick.codegen.SourceCodeGenerator.main(
		Array("slick.jdbc.MySQLProfile", "com.mysql.cj.jdbc.Driver", "core/src/main/scala", "Tables.scala", "valar.core")
	)
}
