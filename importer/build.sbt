name := "valar-importer"

libraryDependencies ++= Seq(
	"com.moandjiezana.toml" % "toml4j" % "0.7.1",
	"com.github.kokellab" %% "skale-chem" % "0.5.0-SNAPSHOT",
	"com.github.kokellab" %% "skale-webservices" % "0.5.0-SNAPSHOT",
	"com.github.kokellab" %% "skale-misc" % "0.5.0-SNAPSHOT",
	"org.apache.ws.commons.axiom" % "axiom-api" % "1.2.19", // apparently needed for ChemSpider API,
	"org.apache.ws.commons.axiom" % "axiom-impl" % "1.2.19" // apparently needed for ChemSpider API,
)
