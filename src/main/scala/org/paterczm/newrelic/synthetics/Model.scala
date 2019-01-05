package org.paterczm.newrelic.synthetics

import org.json4s._
import org.json4s.jackson.JsonMethods._

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read, write, writePretty }
import java.io.File
import scala.io.Source
import java.io.PrintWriter
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.nio.file.Path

case class CustomOptions(labels: Set[String], alertPolicyId: Option[Int])

case class Monitor(var id: Option[String], frequency: Int, locations: Set[String], name: String, uri: String, `options-custom`: CustomOptions, slaThreshold: String, status: String, `type`: String)

case class MonitorsConfig(monitors: Seq[Monitor])

object Monitor {

	val formatWithCustomOptions = DefaultFormats
	val formatWithoutCustomOptions = DefaultFormats + FieldSerializer[Monitor](FieldSerializer.ignore("options-custom"))

	def apply(json: JValue, ignoreCustomOptions: Boolean = false) = {
		implicit val formats = chooseFormat(ignoreCustomOptions)

		json.extract[Monitor]
	}

	def unapply(monitor: Monitor, ignoreCustomOptions: Boolean = false) = {

		implicit val formats = chooseFormat(ignoreCustomOptions)

		writePretty(monitor)
	}

	def chooseFormat(ignoreCustomOptions: Boolean = true) = ignoreCustomOptions match {
		case true => formatWithoutCustomOptions
		case false => formatWithCustomOptions
	}

}

object MonitorsConfig {

	import scala.collection.JavaConversions._

	implicit val formats = Monitor.chooseFormat(false)

	def apply(json: JValue) = json.extract[MonitorsConfig]

	def apply(jsonStr: String): MonitorsConfig = apply(parse(jsonStr))

	def fromFile(path: Path): MonitorsConfig = apply(Files.readAllLines(path).mkString)

	def unapply(monitorsConfig: MonitorsConfig): String = writePretty(monitorsConfig)

	def toFile(monitorsConfig: MonitorsConfig, path: Path): Unit = Files.write(path, unapply(monitorsConfig).getBytes(StandardCharsets.UTF_8))

}