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
class MonitorsTest extends FlatSpec with Matchers {

	val jsonDefinitionOfMonitors = """{
    "monitors": [
        {
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
       },
       {
            "frequency": 10,
            "locations": [
                "123-foo",
                "123-bar"
            ],
            "name": "monitor-name-2",
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
       }
]}"""

	val monitors = MonitorsConfig(jsonDefinitionOfMonitors)

	"monitors.size" should "be 2" in {
		monitors.monitors.size should be(2)
	}
}