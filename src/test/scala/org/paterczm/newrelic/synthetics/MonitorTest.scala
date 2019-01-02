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
class MonitorTest extends FlatSpec with Matchers {

	val jsonDefinitionOfMonitor = """{
            "frequency": 10,
            "locations": [
                "123-foo",
                "123-bar"
            ],
            "name": "monitor-name",
            "options": {},
            "options-custom": {
              "labels": [
                "Environment:Preprod",
                "Team:ThatTeam",
                "Foo:Bar"
              ],
              "alert-policy": "foo"
            },
            "slaThreshold": 7.0,
            "status": "ENABLED",
            "type": "SIMPLE",
            "uri": "https://foo.bar.domain.com"
       }"""

	val json = parse(jsonDefinitionOfMonitor)

	val monitor = Monitor(json)

	"monitor.frequency" should "be 10" in {
		monitor.frequency should be(10)
	}

	"monitor.id" should "be optional" in {
		monitor.id should be(None)
	}

	"monitor.locations" should "should be populated correctly" in {
		monitor.locations.size should be(2)
	}

	"monitor.options-custom" should "include labels and alert policy ref" in {
		monitor.`options-custom`.labels.size should be(3)
		monitor.`options-custom`.`alert-policy` should be("foo")
	}

	"monitor" should "serialize without custom-options when ignoreCustomOptions=true" in {
		val monitorAsJsonString = Monitor.unapply(monitor, true)
		println(monitorAsJsonString)

		parse(monitorAsJsonString) \ "options-custom" should be (JNothing)

	}

	"monitor" should "serialize with custom-options when ignoreCustomOptions=false" in {
		val monitorAsJsonString = Monitor.unapply(monitor, false)
		println(monitorAsJsonString)

		(parse(monitorAsJsonString)) \ "options-custom" should not be (JNothing)
	}
}