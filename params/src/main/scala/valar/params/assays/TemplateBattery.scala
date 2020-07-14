package valar.params.assays

import valar.core._
import valar.core.Tables.{AssayPositionsRow, AssaysRow, BatteriesRow}
import valar.core.loadDb

/**
  * A protocol that was built using template assays.
  */
case class TemplateProtocol(protocol: BatteriesRow, assays: Seq[(AssaysRow, AssayPositionsRow)])

object TemplateProtocols {
	private implicit val db = loadDb()
	import valar.core.Tables._
	import valar.core.Tables.profile.api._

	/**
	  * Lists out all protocols built from only template assays.
	  * @return
	  */
	def list: Seq[TemplateProtocol] = {
		{
			exec({
				for {
					((ainp, p), a) <- AssayPositions join Batteries on (_.batteryId === _.id) join Assays on (_._1.assayId === _.id)
				} yield (a, ainp, p)
			}.result) groupBy (_._3) flatMap { case (protocol: BatteriesRow, group: Seq[(AssaysRow, AssayPositionsRow, BatteriesRow)]) =>
				if (group forall (_._1.templateAssayId.isDefined)) {
					Some(TemplateProtocol(protocol, group map (a => (a._1, a._2))))
				} else None
			}
		}.toSeq groupBy (_.protocol) map { case (protocol, templates) => templates.head }
	}.toSeq

}