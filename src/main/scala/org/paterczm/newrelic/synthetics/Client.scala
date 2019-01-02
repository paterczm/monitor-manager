package org.paterczm.newrelic.synthetics

import scalaj.http.Http
import scalaj.http.HttpResponse
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Client(val apiKey: String) {

	def handleError(res: HttpResponse[String]) = res.code match {
		case s if s >= 200 && s <= 299 => ;
		case s if s >= 400 && s <= 499 =>
			println((parse(res.body) \\ "error").values); System.exit(1)
		case _ => println(s"Error: $res"); System.exit(1)
	}
	
	def deleteMonitor(monitor: Monitor): HttpResponse[String] = deleteMonitor(monitor.id.get)

	def deleteMonitor(uuid: String) = Http(s"https://synthetics.newrelic.com/synthetics/api/v3/monitors/$uuid")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.method("DELETE")
		.asString

	def createPingMonitor(monitor: Monitor) = Http("https://synthetics.newrelic.com/synthetics/api/v3/monitors")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.postData(Monitor.unapply(monitor, true))
		.asString

	def labelMonitor(labels: Set[String], monitorUUID: String) {
		labels.foreach(label => {

			println(s"Creating label $label")

			val res = Http(s"https://synthetics.newrelic.com/synthetics/api/v4/monitors/$monitorUUID/labels")
				.header("X-Api-Key", apiKey)
				.header("Content-Type", "application/json")
				.postData(label)
				.asString

			handleError(res)
		})
	}

	def addNotificationsToMonitor(monitorUUID: String, emails: Set[String]) = Http(s"https://synthetics.newrelic.com/synthetics/api/v1/monitors/$monitorUUID/notifications")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.postData(s"""{"count": ${emails.size}, "emails": ["${emails.mkString("\"")}"]}""")
		.asString

	def createPingMonitorWithCustomOptions(monitor: Monitor) = {

		val res = createPingMonitor(monitor)

		handleError(res)

		val uuid = extractUUID(res)

		println(s"Successfully created monitor uuid=$uuid")

		labelMonitor(monitor.`options-custom`.labels, uuid)

		uuid
	}

	def extractUUID(res: HttpResponse[String]) = {
		val location = res.header("Location").get
		location.substring(location.lastIndexOf('/') + 1)
	}

}