package org.paterczm.newrelic.synthetics

import org.scalatest.Matchers
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.json4s.jackson.JsonMethods._

@RunWith(classOf[JUnitRunner])
class ScriptedMonitorTest extends FlatSpec with Matchers {

	val jsonDefinitionOfScriptedMonitor = """{
            "frequency": 10,
            "locations": [
                "123-foo",
                "123-bar"
            ],
            "name": "monitor-name",
            "options": {},
            "options-custom": {
              "script": {
                "path": "./src/test/resources/script.st",
                "attributes": {
                  "uri": "https://foo.com/health"
                }
              }
            },
            "slaThreshold": 7.0,
            "status": "ENABLED",
            "type": "SIMPLE",
            "uri": "https://foo.bar.domain.com"
       }"""

	val json = parse(jsonDefinitionOfScriptedMonitor)

	val monitor = Monitor(json)

	"monitor.options-custom.script" should "defined" in {
		monitor.`options-custom`.script shouldBe defined
	}

	"monitor.options-custom.script" should "have the uri attribute" in {
		monitor.`options-custom`.script.get.attributes.size shouldBe (1)
		monitor.`options-custom`.script.get.attributes("uri") shouldBe ("https://foo.com/health")
	}

	val expectedScript =
		"""var assert = require('assert');
    |
    |$http.get('https://foo.com/health',
    |  function (err, response, body) {
    |    assert.equal(response.statusCode, 200, 'Expected a 200 response!');
    |  }
    |);""".stripMargin

	"script" should "render" in {
		monitor.`options-custom`.script.get.renderScript() shouldBe (expectedScript)
	}

}