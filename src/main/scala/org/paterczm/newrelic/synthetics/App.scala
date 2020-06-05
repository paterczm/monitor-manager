package org.paterczm.newrelic.synthetics

import java.nio.file.Path
import java.nio.file.Paths
import scalaj.http.Http
import java.net.HttpURLConnection

object App extends App {

	val envApiKey = sys.env.get("NEWRELIC_API_KEY")

	Cli(envApiKey).parse(args, new Cli.Params()) match {
		case Some(cli) => {

			val apiKey = cli.apiKey match {
				case Some(key) => key
				case None => envApiKey match {
					case Some(key) => key
					case None => throw new IllegalArgumentException("New Relic apiKey needs to be provided either by specifying NEWRELIC_API_KEY environment variable or using --apiKey cli parameter")
				}
			}

			val client = new Client(apiKey.trim())
			val manager = new Manager(client)

			cli.action match {
				case "monitors-push" => {

					val config = MonitorsConfig.fromFile(Paths.get(cli.path))

					config.monitors foreach { monitor => manager.createOrUpdateMonitorWithCustomOptions(monitor)	}

					MonitorsConfig.toFile(config, Paths.get(cli.path))
				}
				case "monitors-pull" => {
					println(manager.listMonitorsByLabel(cli.label))
				}
				case "monitors-delete" => {
					manager.deleteMonitor(cli.monitorUuid.get)
				}
				case "locations-pull" => {
					println(manager.listLocations())
				}
				case "alertpolicies-pull" => {
					println(manager.listAlertPolicies())
				}
				case _ => println("Operation not supported")
			}

		}
		case None => ;
	}

}