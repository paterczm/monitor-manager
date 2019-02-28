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
import org.clapper.scalasti.ST

case class Script(path: String, attributes: Map[String, String]) {

	def renderScript() = {

		val scriptTemplate = scala.io.Source.fromFile(path).getLines.mkString("\n")

		val template = ST(scriptTemplate).addAttributes(attributes)

		template.render().get

	}

}

case class CustomOptions(labels: Set[String], alertPolicyId: Option[Int], script: Option[Script])

object MonitorType extends Enumeration {
  type MonitorType = Value
  val SIMPLE, SCRIPT_API = Value
}
import MonitorType.MonitorType

case class Options(validationString: Option[String], verifySSL: Option[Boolean], bypassHeadRequest: Option[Boolean], treatRedirectAsFailure: Option[Boolean])

case class Monitor(var id: Option[String], frequency: Int, locations: Set[String], name: String, uri: Option[String], slaThreshold: String, status: String, `type`: MonitorType, options: Option[Options], `options-custom`: CustomOptions)

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