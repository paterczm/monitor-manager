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
class CliApiKeyTest extends FlatSpec with Matchers {

    def parse(envApiKey: Option[String], onSuccess: () => Unit, onFailure: () => Unit) {
        Cli(envApiKey).parse(List("monitors", "push", "-c", "file"), new Cli.Params()) match {
            case Some(params) => onSuccess()
            case None => onFailure()
        }
    }

    "apiKey" should "be required" in {
        parse(None, () => Assert.fail(), () => Unit)
        parse(Some("foo"), () => Unit, () => Assert.fail())
    }

}