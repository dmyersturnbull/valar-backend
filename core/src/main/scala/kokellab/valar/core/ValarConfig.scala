package kokellab.valar.core

import java.time.ZoneId
import slick.jdbc.JdbcBackend.Database
import System.err.{println => printerrln}
import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

class ValarConfig(val config: Config = ConfigFactory.parseFile(new File("conf/application.conf"))) {

	val timezone: ZoneId = ZoneId.systemDefault()
	val chemspiderToken: String = config.getString("chemspiderToken")

	private lazy val db: Database = try {
		Database.forConfig("valar_db", config)
	} catch {
		case e: Throwable =>
			// Guice injection in Valinor throws a confusion error message otherwise
			printerrln("ERROR: ValarConfig.load() failed")
			throw e
	}
	def load = db

}

/**
  * Mutable singleton.
  */
object ValarConfig {

	var instance: ValarConfig = new ValarConfig()

	def main(args: Array[String]): Unit = {
		implicit val db = instance.db
		import kokellab.valar.core.Tables._
		import kokellab.valar.core.Tables.profile.api._
		println("Users: " + exec((
			Users map (_.username)
		).result).mkString(", "))
	}
}
