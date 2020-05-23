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

object CompoundLabelInsertion {

	val logger: Logger = Logger(getClass)
	private implicit val db = loadDb()

	import kokellab.valar.core.Tables._
	import kokellab.valar.core.Tables.profile.api._

	def insert(data: CompoundLabelData, refId: Byte): CompoundLabelsRow = attempt { () => {
		val result = exec((CompoundLabels filter (_.refId === data.refId) filter (_.compoundId === data.compoundId) filter (_.name === data.name)).result).headOption
		result getOrElse {
			val query = CompoundLabels returning (CompoundLabels map (_.id)) into ((newRow, id) => newRow.copy(id = id))
			exec(query += CompoundLabelsRow(
				id = 0,
				refId = data.refId,
				compoundId = data.compoundId,
				name = data.name,
				created = timestamp()
			))
		}
	}}

}
