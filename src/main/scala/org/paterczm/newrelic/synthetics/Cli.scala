package org.paterczm.newrelic.synthetics

object Cli {

	case class Params(apiKey: Option[String], path: String, action: String, label: String, monitorUuid: Option[String]) {
		def this() = this(None, null, null, null, None)
	}

    def apply(envApiKey: Option[String]) = {

        new scopt.OptionParser[Params]("monitor-manager") {
            head("Command line tool to manage New Relic Synthetics Monitors")

            opt[String]('k', "apiKey").optional().valueName("Synthetics apiKey (see https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys)")
                .action((str, params) => params.copy(apiKey = Some(str)))

            cmd("monitors")
                .text("Monitor related operations")
                .children(
                    cmd("push").action((_, params) => params.copy(action = "monitors-push"))
                        .text("Create monitors. If monitor ids are provided, it will update existing monitors.")
                        .children(
                            opt[String]('c', "monitorsConfigFile").required().valueName("Path to config file with monitors. See MonitorsTest.scala for expected structure.")
                                .action((str, params) => params.copy(path = str))
                        ),
                    cmd("pull").action((_, params) => params.copy(action = "monitors-pull"))
                        .text("List monitors by label")
                        .children(
                            opt[String]('l', "label").required().valueName("Label, e.g. Team:Paas")
                                .action((str, params) => params.copy(label = str))
                        ),
                    cmd("delete").action((_, params) => params.copy(action = "monitors-delete"))
                        .text("Delete monitor by id")
                        .children(
                            opt[String]('i', "id").required().valueName("Monitor id")
                                .action((str, params) => params.copy(monitorUuid = Some(str)))
                        )
                )

            cmd("locations")
                .text("Location related operations")
                .children(
                    cmd("pull").action((_, params) => params.copy(action = "locations-pull"))
                        .text("Pull a list of available monitors")
            )

            cmd("alertpolicies")
                .text("Alert Policy related operations")
                .children(
                    cmd("pull").action((_, params) => params.copy(action = "alertpolicies-pull"))
                        .text("Pull all alert policies")
            )

            checkConfig( params =>
                if (!params.apiKey.isEmpty || !envApiKey.isEmpty) success else failure("New Relic apiKey needs to be provided either by specifying NEWRELIC_API_KEY environment variable or using --apiKey cli parameter") )
        }
    }

}