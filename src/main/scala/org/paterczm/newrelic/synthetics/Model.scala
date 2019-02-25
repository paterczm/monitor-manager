package org.paterczm.newrelic.synthetics

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import org.json4s.DefaultFormats
import org.json4s.FieldSerializer
import org.json4s.JValue
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import org.json4s.jvalue2extractable
import org.json4s.string2JsonInput

case class CustomOptions(labels: Set[String], alertPolicyId: Option[Int], scriptLines: Option[Seq[String]]) {
	def script = scriptLines match {
		case Some(lines) => Some(lines.mkString("\n"))
		case None => None
	}
}

object MonitorType extends Enumeration {
  type MonitorType = Value
  val SIMPLE, SCRIPT_API = Value
}
import MonitorType.MonitorType

case class Monitor(var id: Option[String], frequency: Int, locations: Set[String], name: String, uri: Option[String], `options-custom`: CustomOptions, slaThreshold: String, status: String, `type`: MonitorType)

case class MonitorsConfig(monitors: Seq[Monitor])

object Monitor {

	val formatWithCustomOptions = DefaultFormats + new EnumNameSerializer(MonitorType)
	val formatWithoutCustomOptions = formatWithCustomOptions + FieldSerializer[Monitor](FieldSerializer.ignore("options-custom"))

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