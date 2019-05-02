package org.paterczm.newrelic.synthetics

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.junit.Test
import org.junit.Assert
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read, write }

@RunWith(classOf[JUnitRunner])
class CliTest extends FlatSpec with Matchers {

	"apiKey" should "is required" in {
		Cli.parser.parse(List("monitors", "push", "-c", "file"), new Cli.Params()) match {
            case Some(cli) => {
                Assert.fail("Missing apiKey should not be accepted")
            }
            case None => ;
        }
	}
}