package kokellab.valar.core
import java.util.concurrent.TimeUnit
import slick.jdbc.MySQLProfile

import slick.codegen.SourceCodeGenerator
import slick.model

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object SchemaGenerator {

	def main(args: Array[String]): Unit = {
		val config = ValarConfig.instance
		implicit val db = config.load
		val url = config.config.getString("valar_db.url")
		val gen = SourceCodeGenerator.run(
			"slick.jdbc.MySQLProfile",
			"com.mysql.cj.jdbc.Driver",
			url,
			"core/src/main/scala",
			"kokellab.valar.core",
			None,
			None,
		true
		)
	}

}
