package kokellab.valar.params.assays

import com.typesafe.scalalogging.Logger
import kokellab.utils.grammars.params._
import kokellab.valar.core.CommonQueries.{listStimuli, listTemplateStimulusFrames}
import kokellab.valar.core.Tables.TemplateAssaysRow
import kokellab.valar.core.loadDb
import kokellab.valar.params.{AssayParam, ParamOrigin}

/**
  * Utilities that work on parameters, prior to parameterization with values.
  */
object AssayParameters {

	val logger: Logger = Logger(getClass)
	private implicit val db = loadDb()

	/**
	  * Fetches assay parameters from Valar for a given TemplateAssay.
	  */
	def assayParams(templateAssay: TemplateAssaysRow): Seq[AssayParam] =
		assayParams(templateAssay.id)

	def assayParams(templateAssayId: Int): Seq[AssayParam] = {
		val frames = listTemplateStimulusFrames filter (_.templateAssayId == templateAssayId)
		frames flatMap {t =>
			val isAnalog = (listStimuli filter (_.id == t.stimulusId)).head.analog
			val range: Set[AssayParam] = DollarSignParams.find(t.rangeExpression, Set.empty) map (p => AssayParam(p, ParamOrigin.assayRange))
			val value: Set[AssayParam] = DollarSignParams.find(t.valueExpression, Set("$t")) map (p => AssayParam(p, if (isAnalog) ParamOrigin.pwm else ParamOrigin.digital))
			(range ++ value) filterNot (_.param.isArrayAccess) filterNot (_.param.isPredefined)
		}
	}

	private def originTypeFromExpression(s: String) = ???  // TODO

}
