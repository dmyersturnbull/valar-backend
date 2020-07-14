package valar.core

import valar.core.Tables.Runs
import org.scalatest.{Matchers, PropSpec}

class ImageStoreTest extends PropSpec with Matchers {

	implicit val db = loadDb()
	import valar.core.Tables._
	import valar.core.Tables.profile.api._

	property("Lookups are correct") {
		// TODO depends on real data
		val path = ImageStore.pathOf(exec((Runs filter (_.id === 1)).result).head)
		println(path)
		path should equal ("/data/2016/06/1")
	}

}
