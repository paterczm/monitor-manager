package org.paterczm.newrelic.synthetics

import scalaj.http.Http
import scalaj.http.HttpResponse
import org.json4s._
import org.json4s.jackson.JsonMethods._

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read, write, writePretty }

class Manager(client: Client) {

	implicit val format = DefaultFormats

	def createOrUpdateMonitorWithCustomOptions(monitor: Monitor) = {

		monitor.id match {
			case None => {
				val res = client.createMonitor(monitor)

				handleError(res)

				val uuid = extractUUID(res)

				println(s"Successfully created monitor uuid=$uuid")

				if (monitor.`options-custom`.isDefined) {
				  labelMonitor(monitor.`options-custom`.get.labels, uuid)
				}

				monitor.id = Some(uuid)

				handleCustomOptions(monitor, true)

				uuid
			}
			case Some(uuid) => {

				val res = client.updateMonitor(monitor)

				handleError(res)

				println(s"Successfully updated monitor uuid=$uuid")

				if (monitor.`options-custom`.isDefined) {
				  labelMonitor(monitor.`options-custom`.get.labels, uuid)
				}

				handleCustomOptions(monitor, false)

				uuid
			}
		}
	}

	// TODO: this should return actual monitors, not just refs :/
	def listMonitorsByLabel(label: String) = {

		val res = client.listMonitorsByLabel(label)

		handleError(res)

		writePretty(parse(res.body))
	}

	def labelMonitor(labels: Set[String], monitorUUID: String) {
		labels.foreach(label => {

			println(s"Creating label $label")

			val res = client.addLabelToMonitor(label, monitorUUID)

			handleError(res)
		})
	}

	def deleteMonitor(uuid: String) {
		val res = client.deleteMonitor(uuid)

		handleError(res)
	}

	def listLocations() = {
		val res = client.listLocations()

		handleError(res)

		writePretty(parse(res.body))
	}

	def listAlertPolicies() = {
		val res = client.listAlertPolicies()

		handleError(res)

		writePretty(parse(res.body))
	}

	private def extractUUID(res: HttpResponse[String]) = {
		val location = res.header("Location").get
		location.substring(location.lastIndexOf('/') + 1)
	}

	private def handleError(res: HttpResponse[String]) = res.code match {
		case s if s >= 200 && s <= 299 => ;
		case s if s >= 400 && s <= 499 =>
			println((parse(res.body) \\ "error").values); System.exit(1)
		case _ => println(s"Error: $res"); System.exit(1)
	}

	private def handleCustomOptions(monitor: Monitor, createAlertCondition: Boolean) {

		if (monitor.`options-custom`.isDefined) {

		monitor.`options-custom`.get.alertPolicyId match {
			case None => ;
			case Some(x) if !createAlertCondition => ; // don't create alert policy on update
			case Some(x) if createAlertCondition => {
				val acres = client.createAlertCondition(monitor)

				handleError(acres)
			}
		}

		monitor.`options-custom`.get.script match {
			case None => ;
			case Some(script) => {
				val scres = client.updateScriptOnMonitor(monitor.id.get, script.renderScript())

				handleError(scres)
			}
		}
		}

	}

}