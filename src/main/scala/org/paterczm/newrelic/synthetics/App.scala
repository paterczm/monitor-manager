package org.paterczm.newrelic.synthetics

import java.nio.file.Path
import java.nio.file.Paths
import scalaj.http.Http
import java.net.HttpURLConnection

object App extends App {

	case class Cli(apiKey: String, path: String, action: String) {
		def this() = this(null, null, null)
	}

	val parser = new scopt.OptionParser[Cli]("monitor-manager") {
		head("Command line tool to manage New Relic Synthetics Monitors")

		opt[String]('k', "apiKey").required().valueName("Synthetics apiKey (see https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys)")
			.action((str, cli) => cli.copy(apiKey = str))

		cmd("push").action((_, cli) => cli.copy(action = "push"))
			.text("Create monitors. If monitor ids are provided, it will overwrite existing monitors.")
			.children(
				opt[String]('c', "monitorsConfigFile").required().valueName("Path to config file with monitors. See MonitorsTest.scala for expected structure.")
					.action((str, cli) => cli.copy(path = str)))
	}

	parser.parse(args, new Cli()) match {
		case Some(cli) => {

			val client = new Client(cli.apiKey)

			cli.action match {
				case "push" => {

					val config = MonitorsConfig.fromFile(Paths.get(cli.path))

					config.monitors foreach { monitor =>
						{
							monitor.id match {
								case None => {
									val uuid = client.createMonitorWithCustomOptions(monitor)
									monitor.id = Some(uuid)
								}
								case Some(uuid) => {
									println("Updates are not supported yet")
								}
							}

							MonitorsConfig.toFile(config, Paths.get(cli.path))
						}
					}
				}
			}

		}
		case None => ;
	}

}