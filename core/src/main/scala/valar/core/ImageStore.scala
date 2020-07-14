package valar.core

import java.nio.file.{Files, Path, Paths}
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import com.typesafe.scalalogging.LazyLogging
import valar.core.Tables.RunsRow

trait GenImageStore {
	def pathOf(run: RunsRow): Path
	def walk(run: RunsRow): Iterator[Path]
}

object ImageStore extends GenImageStore with LazyLogging {

	implicit val db = loadDb()
	import valar.core.Tables._
	import valar.core.Tables.profile.api._

	val defaultFilenameExtensions = Set(".jpg")

	val root = Paths.get("/data/plates")

	/**
	  * This will <strong>not</strong> work during import, before the data has been copied.
	  */
	def pathOf(run: RunsRow): Path =
		root.resolve("by-id").resolve(run.id.toString).resolve("frames")

	/**
	  * This will work during import, before the data has been copied.
	  */
	def pathOf(submission: SubmissionsRow): Path =
		root.resolve("by-hash").resolve(submission.lookupHash).resolve("frames")

	/**
	  * Returns the number of frames.
	  */
	def length(run: RunsRow): Int = walk(run).size

	def walk(run: RunsRow): Iterator[Path] = {
		val path = pathOf(run)
		val stream = if (run.submissionId.isDefined) {
			Files.list(path).iterator().asScala.toStream
		} else {
			Files.walk(path).iterator().asScala.toStream filter (_.iterator.asScala exists (_ endsWith "-JPGs")) // could be improved
		}
		filteredAndSorted(stream).iterator
	}

	private def filteredAndSorted(stream: Stream[Path], filenameExtensions: Set[String] = defaultFilenameExtensions): Stream[Path] = (
		stream
			filter (p => filenameExtensions exists (p.toString.endsWith(_)))
			sortBy (_.normalize().toString)
		)

}
