package org.paterczm.newrelic.synthetics

import scalaj.http.Http
import scalaj.http.HttpResponse
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Manager(client: Client) {

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

				uuid
			}
		}
	}

	def labelMonitor(labels: Set[String], monitorUUID: String) {
		labels.foreach(label => {

			println(s"Creating label $label")

			val res = client.addLabelToMonitor(label, monitorUUID)

			handleError(res)
		})
	}

	def extractUUID(res: HttpResponse[String]) = {
		val location = res.header("Location").get
		location.substring(location.lastIndexOf('/') + 1)
	}

	def handleError(res: HttpResponse[String]) = res.code match {
		case s if s >= 200 && s <= 299 => ;
		case s if s >= 400 && s <= 499 =>
			println((parse(res.body) \\ "error").values); System.exit(1)
		case _ => println(s"Error: $res"); System.exit(1)
	}

}