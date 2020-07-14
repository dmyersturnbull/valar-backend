scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
	"com.typesafe" % "config" % "1.4.0",
	"com.typesafe.slick" %% "slick" % "3.3.2",
	"com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
	"com.typesafe.slick" %% "slick-codegen" % "3.3.2",
	"mysql" % "mysql-connector-java" % "8.0.20"
)
