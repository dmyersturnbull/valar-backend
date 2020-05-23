package kokellab.valar.importer.extras


import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.LazyLogging
import kokellab.valar.core.DateTimeUtils._
import kokellab.valar.core.{exec, execInsert, loadDb}
import kokellab.utils.core.exceptions.InvalidDataFormatException
import kokellab.utils.core.{blobToHex, bytesToBlob, bytesToHex, withLoggedError}
import kokellab.utils.misc.AudioFile
import slick.jdbc.JdbcBackend.Database

import scala.language.postfixOps

@deprecated("This class doesn't support WAV files. Use valarpy instead.", "0.4.0")
class AudioFileLoader(implicit database: Database) extends AnyRef with LazyLogging {

	import kokellab.valar.core.Tables._
	import kokellab.valar.core.Tables.profile.api._

	def loadAllFiles(dir: Path) : Unit= {
		val fileList = Option(Files.list(dir)).getOrElse(throw new IllegalArgumentException(s"Could not list files in $dir"))
		fileList.collect(Collectors.toList()).asScala filter (f => f.endsWith(".mp3") || f.endsWith(".wav")) foreach loadFile
	}

	def loadFile(file: Path): Unit = withLoggedError(s"Failed to load file $file", () => {

		val audio = AudioFile.read(file)

		val found = exec((
			AudioFiles filter (_.filename === file.getFileName.toString) map (r => (r.id, r.sha1))
		).result).headOption

		// insert if the name is new
		// otherwise, complain if the hashes don't match
		if (found.isEmpty) {

			execInsert(
				AudioFiles += AudioFilesRow(0, file.getFileName.toString, None, audio.nSeconds, bytesToBlob(audio.bytes), bytesToBlob(audio.sha1), None, timestamp())
			)
//			checkInsert("AudioFiles", exec(
//				AudioFiles += AudioFilesRow(0, file.getFileName.toString, None, audio.nSeconds, bytesToBlob(audio.bytes), bytesToBlob(audio.sha1), None, timestamp())
//			))
			logger.info(s"Inserted ${file.toString} with ${Files.size(file)} bytes for ${audio.nSeconds} seconds (${bytesToHex(audio.sha1)})")
		} else {
			val oldId = found.get._1
			val oldHex = blobToHex(found.get._2)
			val newHex = bytesToHex(audio.sha1)
			if (oldHex == newHex) {
				logger.debug(s"Audio file ${file.getFileName.toString} already exists with ID $oldId and the same hash ($newHex); skipping")
			} else {
				throw new InvalidDataFormatException(s"Audio file with name ${file.getFileName.toString} already exists with ID $oldId and a different hash (in DB: $oldHex; in $file: $newHex)")
			}
		}
	})

}

object AudioFileLoader {
	def main(args: Array[String]): Unit = {
		implicit val db = loadDb()
		try {
			new AudioFileLoader().loadAllFiles(Paths.get(args(0)))
		} finally db.close()
	}
}
