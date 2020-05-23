package kokellab.valar.importer

import kokellab.valar.core.{exec, loadDb}

abstract class Processor(submissionResult: SubmissionResult) {

	private implicit val db = loadDb()
	import kokellab.valar.core.Tables._
	import kokellab.valar.core.Tables.profile.api._

	def apply(plateRun: RunsRow): Unit
}

