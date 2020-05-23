package kokellab.valar.insertion

import com.typesafe.scalalogging.Logger
import kokellab.valar.core.DateTimeUtils.timestamp
import kokellab.valar.core.{exec, loadDb}


object ProjectInsertion {

	val logger: Logger = Logger(getClass)
	private implicit val db = loadDb()

	import kokellab.valar.core.Tables._
	import kokellab.valar.core.Tables.profile.api._

	def insert(data: SuperprojectData): SuperprojectsRow = attempt { () => {
		val projectType = exec((ProjectTypes filter (_.id === data.projectType)).result).head
		if (!(data.name startsWith (projectType.name + " :: "))) {
			throw new ValidationException(s"The project name must begin with ${projectType.name + " :: "}")
		}
		val query = Superprojects returning (Superprojects map (_.id)) into ((newRow, id) => newRow.copy(id = id))
		exec(query += SuperprojectsRow(
			id = 0,
			name = data.name,
			typeId = Some(data.projectType),
			creatorId = data.creator,
			description = data.description,
			reason = data.reason,
			methods = data.methods,
			created = timestamp()
		))
	}}

}