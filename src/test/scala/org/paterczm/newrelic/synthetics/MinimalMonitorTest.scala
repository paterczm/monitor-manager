package org.paterczm.newrelic.synthetics

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import org.scalatest.FlatSpec

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read, write }

@RunWith(classOf[JUnitRunner])
class MinimalMonitorTest extends FlatSpec with Matchers {

	val jsonDefinitionOfMonitor = """
        {
            "frequency": 10,
            "locations": [
              "foo"
            ],
            "name": "example-newrelicmonitor2",
            "slaThreshold": 7.0,
            "status": "ENABLED",
            "type": "SIMPLE",
            "uri": "https://www.redhat.com"
       }}
"""

	val json = parse(jsonDefinitionOfMonitor)

	val monitor = Monitor(json)

	"monitor.frequency" should "be 10" in {
		monitor.frequency should be(10)
	}

	"monitor" should "not serialize custom-options when ignoreCustomOptions=false and custom-options are missing" in {
		val monitorAsJsonString = Monitor.unapply(monitor, false)
		println(monitorAsJsonString)

		(parse(monitorAsJsonString)) \ "options-custom" should be (JNothing)
	}

}