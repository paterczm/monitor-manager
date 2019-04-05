package org.paterczm.newrelic.synthetics

import java.nio.file.Path
import java.nio.file.Paths
import scalaj.http.Http
import java.net.HttpURLConnection

object App extends App {

	case class Cli(apiKey: String, path: String, action: String, label: String, monitorUuid: Option[String]) {
		def this() = this(null, null, null, null, None)
	}

	val parser = new scopt.OptionParser[Cli]("monitor-manager") {
		head("Command line tool to manage New Relic Synthetics Monitors")

		opt[String]('k', "apiKey").required().valueName("Synthetics apiKey (see https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys)")
			.action((str, cli) => cli.copy(apiKey = str))

		cmd("monitors")
			.text("Monitor related operations")
			.children(
				cmd("push").action((_, cli) => cli.copy(action = "monitors-push"))
					.text("Create monitors. If monitor ids are provided, it will update existing monitors.")
					.children(
						opt[String]('c', "monitorsConfigFile").required().valueName("Path to config file with monitors. See MonitorsTest.scala for expected structure.")
							.action((str, cli) => cli.copy(path = str))
					 ),
				cmd("pull").action((_, cli) => cli.copy(action = "monitors-pull"))
					.text("List monitors by label")
					.children(
						opt[String]('l', "label").required().valueName("Label, e.g. Team:Paas")
							.action((str, cli) => cli.copy(label = str))
					),
				cmd("delete").action((_, cli) => cli.copy(action = "monitors-delete"))
					.text("Delete monitor by id")
					.children(
						opt[String]('i', "id").required().valueName("Monitor id")
							.action((str, cli) => cli.copy(monitorUuid = Some(str)))
					)
			)

		cmd("locations")
			.text("Location related operations")
			.children(
				cmd("pull").action((_, cli) => cli.copy(action = "locations-pull"))
					.text("Pull a list of available monitors")
		)

		cmd("alertpolicies")
			.text("Alert Policy related operations")
			.children(
				cmd("pull").action((_, cli) => cli.copy(action = "alertpolicies-pull"))
					.text("Pull all alert policies")
		)
	}

	parser.parse(args, new Cli()) match {
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