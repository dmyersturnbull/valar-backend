scalaVersion := "2.12.2"

crossScalaVersions := Seq(scalaVersion.value, "2.11.8")

libraryDependencies ++= Seq(
	"com.typesafe" % "config" % "1.3.0",
	"com.typesafe.slick" %% "slick" % "3.2.0",
	"com.typesafe.slick" %% "slick-codegen" % "3.2.0",
	"com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
	"mysql" % "mysql-connector-java" % "6.0.6"
)
