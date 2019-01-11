package org.paterczm.newrelic.synthetics

import scalaj.http.Http
import scalaj.http.HttpResponse
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Client(val apiKey: String) {

	val headers = Seq(("X-Api-Key", apiKey), ("Content-Type", "application/json"), ("Accept", "application/json"))

	def deleteMonitor(uuid: String) = Http(s"https://synthetics.newrelic.com/synthetics/api/v3/monitors/$uuid")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.method("DELETE")
		.asString

	def createMonitor(monitor: Monitor) = Http("https://synthetics.newrelic.com/synthetics/api/v3/monitors")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.postData(Monitor.unapply(monitor, true))
		.asString

	// https://docs.newrelic.com/docs/apis/synthetics-rest-api/monitor-examples/manage-synthetics-monitors-rest-api#update-monitor
	def updateMonitor(monitor: Monitor) = Http(s"https://synthetics.newrelic.com/synthetics/api/v3/monitors/${monitor.id.get}")
		.headers(headers)
		.put(Monitor.unapply(monitor, true))
		.asString

	// https://docs.newrelic.com/docs/apis/synthetics-rest-api/label-examples/use-synthetics-label-apis
	// the api does not provide means to update labels
	def addLabelToMonitor(label: String, monitorUUID: String) = Http(s"https://synthetics.newrelic.com/synthetics/api/v4/monitors/$monitorUUID/labels")
				.header("X-Api-Key", apiKey)
				.header("Content-Type", "application/json")
				.postData(label)
				.asString

	def addLegacyNotificationsToMonitor(monitorUUID: String, emails: Set[String]) = Http(s"https://synthetics.newrelic.com/synthetics/api/v1/monitors/$monitorUUID/notifications")
		.header("X-Api-Key", apiKey)
		.header("Content-Type", "application/json")
		.postData(s"""{"count": ${emails.size}, "emails": ["${emails.mkString("\"")}"]}""")
		.asString

	// https://rpm.newrelic.com/api/explore/alerts_synthetics_conditions/create
	def createAlertCondition(monitor: Monitor) = Http(s"https://api.newrelic.com/v2/alerts_synthetics_conditions/policies/${monitor.`options-custom`.alertPolicyId.get}.json")
		.headers(headers)
		.postData(s"""{
  "synthetics_condition": {
    "name": "${monitor.name}",
    "monitor_id": "${monitor.id.get}",
    "enabled": true
  }
}""").asString

}