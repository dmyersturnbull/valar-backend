package valar.core

trait FileFormat {
	val name: String
	val fullName: String
	val extensions: Set[String]
}
trait Codec extends FileFormat {
	val lossless: Boolean
}
trait ContainerFormat extends FileFormat


sealed trait ImageCodec extends Codec

object ImageCodec {
	val png = new ImageCodec {
		override val name: String = "png"
		override val fullName: String = "Portable Network Graphics"
		override val extensions = Set("png")
		override val lossless: Boolean = true
	}
	val jpeg = new ImageCodec {
		override val name: String = "jpeg"
		override val fullName: String = "Joint Photographic Experts Group"
		override val extensions = Set("jpg", "jpeg")
		override val lossless: Boolean = false
	}
}

sealed trait AudioCodec extends Codec
object AudioCodec {
	val mp3 = new AudioCodec {
		override val name: String = "mp3"
		override val fullName: String = "Moving Picture Experts Group Layer-3 Audio"
		override val extensions = Set("mp3")
		override val lossless: Boolean = false
	}
	val wav = new AudioCodec {
		override val name: String = "wave"
		override val fullName: String = "Waveform Audio File Format"
		override val extensions = Set("wav", "wave")
		override val lossless: Boolean = true
	}
}

sealed trait VideoCodec extends Codec
object VideoCodec {
	val h264 = new VideoCodec {
		override val name: String = "h.264"
		override val fullName: String = "MPEG-4 Part 10, Advanced Video Coding (MPEG-4 AVC)"
		override val extensions = Set("h264", "h.264")
		override val lossless: Boolean = false
	}
}

sealed trait VideoContainerFormat extends ContainerFormat
object VideoContainerFormat {
	val ogg = new VideoContainerFormat {
		override val name: String = "ogg"
		override val fullName: String = "Ogg"
		override val extensions = Set("ogg", "ogv")
	}
	val mp4 = new VideoContainerFormat {
		override val name: String = "mp4"
		override val fullName: String = "MPEG-4 Part 14"
		override val extensions = Set("mp4", "m4p", "m4v")
	}
}
