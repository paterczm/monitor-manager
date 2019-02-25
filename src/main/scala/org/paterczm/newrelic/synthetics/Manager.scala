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

				labelMonitor(monitor.`options-custom`.labels, uuid)

				monitor.id = Some(uuid)

				monitor.`options-custom`.alertPolicyId match {
					case None => ;
					case Some(x) => {
						val acres = client.createAlertCondition(monitor)

						handleError(acres)
					}
				}

				uuid
			}
			case Some(uuid) => {
				val res = client.updateMonitor(monitor)

				handleError(res)

				println(s"Successfully updated monitor uuid=$uuid")

				labelMonitor(monitor.`options-custom`.labels, uuid)

				monitor.`options-custom`.alertPolicyId match {
					case None => ;
					case Some(x) => {
						val acres = client.createAlertCondition(monitor)

						handleError(acres)
					}
				}

				monitor.`options-custom`.script match {
					case None => ;
					case Some(script) => {
						val scres = client.updateScriptOnMonitor(uuid, script)

						handleError(scres)
					}
				}

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

}