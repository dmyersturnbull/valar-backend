name := "valar-insertion"
libraryDependencies ++= Seq(
        "org.apache.axis2" % "axis2" % "1.7.7" pomOnly(), // apparently needed for ChemSpider API
        "org.apache.axis2" % "axis2-kernel" % "1.7.7", // apparently needed for ChemSpider API
        "org.apache.axis2" % "axis2-adb" % "1.7.7", // apparently needed for ChemSpider API
        "org.apache.axis2" % "axis2-transport-local" % "1.7.7", // apparently needed for ChemSpider API
        "org.apache.axis2" % "axis2-transport-http" % "1.7.7", // apparently needed for ChemSpider API
        "org.apache.axis2" % "axis2-jaxws" % "1.7.7", // apparently needed for ChemSpider API,
        "org.apache.ws.commons.axiom" % "axiom-api" % "1.2.20", // apparently needed for ChemSpider API,
        "org.apache.ws.commons.axiom" % "axiom-impl" % "1.2.20" // apparently needed for ChemSpider API,
)// map (_.exclude("org.apache.ws.commons.axiom", "axiom-impl"))

libraryDependencies ++= Seq(
	"com.github.kokellab" %% "skale-chem" % "0.5.0-SNAPSHOT",
	"com.github.kokellab" %% "skale-webservices" % "0.5.0-SNAPSHOT"
)
