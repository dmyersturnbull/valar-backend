package kokellab.valar.insertion

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import com.typesafe.scalalogging.Logger
import kokellab.utils.chem.Chem
import kokellab.utils.core.{bytesToHashHex, intsToBytes}
import kokellab.utils.webservices.Chemspider
import kokellab.utils.core
import kokellab.utils.core.addons.SecureRandom
import kokellab.valar.core.DateTimeUtils.timestamp
import kokellab.valar.core.{DateTimeUtils, ValarConfig, exec, loadDb}

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

object Csids {

	val logger: Logger = Logger(getClass)
	private implicit val db = loadDb()

	import kokellab.valar.core.Tables._
	import kokellab.valar.core.Tables.profile.api._

        def main(args: Array[String]) = {
            for (compound <- exec(Compounds.result)) {
                if (compound.chemspiderId.isEmpty) {
                    val csid = getChemspiderId(compound.inchi)
                    if (csid.nonEmpty) {
                    println(s"Updated ${compound.id} with $csid")
                    exec(Compounds filter (_.id === compound.id) map (_.chemspiderId) update (csid))
                    }
                }
            }
        }
	private def getChemspiderId(inchi: String): Option[Int] = {
		Try(new Chemspider(ValarConfig.instance.chemspiderToken).fetchChemspiderIds(inchi)) map core.only
	} match {
		case Success(id: Int) => Some(id)
		case Failure(e) =>
			logger.warn(s"No ChemSpider IDs found for Inchi ${inchi}", e)
			None
	}

}
