import com.typesafe.config.{ConfigException, ConfigFactory, Config}

name := "valar"

description := "Infrastructure supporting Valar, the all-knowing relational database"

lazy val commonSettings = Seq(
	organization := "com.github.dmyersturnbull",
	organizationHomepage := Some(url("https://github.com/dmyersturnbull")),
	version := "1.0.0-SNAPSHOT",
	isSnapshot := true,
	scalaVersion := "2.13.3",
	javacOptions ++= Seq("-source", "14", "-target", "14", "-Xlint:all"),
	javaOptions += "-Xmx4G",
	scalacOptions ++= Seq("-unchecked", "-deprecation"),
	testOptions in Test += Tests.Argument("-oF"),
	homepage := Some(url("https://github.com/dmyersturnbull/valar-infrastructure")),
	developers := List(Developer("dmyersturnbull", "Douglas Myers-Turnbull", "---", url("https://github.com/dmyersturnbull"))),
	startYear := Some(2016),
	scmInfo := Some(ScmInfo(url("https://github.com/dmyersturnbull/valar-infrastructure"), "https://github.com/dmyersturnbull/valar-infrastructure.git")),
	libraryDependencies ++= Seq(
		"com.iheart" %% "ficus" % "1.4.7",
		"com.typesafe.slick" %% "slick" % "3.3.2",
		"com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
		"com.typesafe.slick" %% "slick-codegen" % "3.3.2",
		"mysql" % "mysql-connector-java" % "8.0.20",
		"com.github.scopt" %% "scopt" % "4.0.0-RC2",
		"io.argonaut" %% "argonaut" % "6.3.0",
		"de.svenkubiak" % "jBCrypt" % "0.4.1",
		"com.google.code.findbugs" % "jsr305" % "3.0.1", // to work around compiler warnings about missing annotations from Guava
		"com.typesafe" % "config" % "1.4.0",
		"com.google.guava" % "guava" % "29.0-jre",
		"org.slf4j" % "slf4j-api" % "2.0.0-alpha1",
		"com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
		"org.scalatest" %% "scalatest" % "3.2.0" % "test",
		"org.scalactic" %% "scalactic" % "3.2.0" % "test",
		"org.scalacheck" %% "scalacheck" % "1.14.3" % "test",
		"org.scalatestplus" %% "scalacheck-1-14" % "3.2.0.0" % "test"
	) map (_.exclude("org.slf4j", "slf4j-log4j12")),
	dependencyOverrides += "com.google.guava" % "guava" % "25.1-jre", // some dependency uses 17.0
	dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.8.0-beta2" // can't use 1.7.x is incompatible
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
