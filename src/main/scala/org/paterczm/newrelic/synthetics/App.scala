package org.paterczm.newrelic.synthetics

import java.nio.file.Path
import java.nio.file.Paths
import scalaj.http.Http
import java.net.HttpURLConnection

object App extends App {

	Cli.parser.parse(args, new Cli.Params()) match {
		case Some(cli) => {

			val client = new Client(cli.apiKey)
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