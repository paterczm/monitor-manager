# monitor-manager
Command line tool to manage New Relic Synthetics Monitors

## Obtaining new relic api key

To use this tool, new need to have a new relic account and [api keys](https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys).

## Installation

Requires java 8 or later to run.

To install or update:
```
$ curl -L https://github.com/paterczm/monitor-manager/raw/master/bin/monitor-manager-latest.jar > ~/bin/monitor-manager.jar && curl -L https://raw.githubusercontent.com/paterczm/monitor-manager/master/bin/monitor-manager > ~/bin/monitor-manager && chmod +x ~/bin/monitor-manager

# Test
$ monitor-manager
Error: Missing option --apiKey
Command line tool to manage New Relic Synthetics Monitors
Usage:
(...)

```

## Usage

For complete command line syntax, print usage:

```
monitor-manager
```

Monitors definition looks like this:

```json
{
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
              "alertPolicyId": 1
            },
            "slaThreshold": 7.0,
            "status": "ENABLED",
            "type": "SIMPLE",
            "uri": "https://foo.bar.domain.com"
       }
	]
}
```

Those are all standard parameters from [new relic synthetics api](https://docs.newrelic.com/docs/apis/synthetics-rest-api/monitor-examples/manage-synthetics-monitors-rest-api), except for options-custom element, defining labels (using [new relic synthetics labels api](https://docs.newrelic.com/docs/apis/synthetics-rest-api/label-examples/use-synthetics-label-apis) ) and an alert policy (using [alert synthetics conditions api](https://rpm.newrelic.com/api/explore/alerts_synthetics_conditions/create)).

To create monitors:

```
monitor-manager monitors push --monitorsConfigFile monitors.json --apiKey <api key>
```

The monitors config file contains references to locations and alert policy. You will need to create them manually and figure out what their ids are. The tool does not help with that right now.

For each created monitor, the tool will update monitors config file with monitor ids, allowing for future updates to existing monitors. To update a monitor, simply edit the config file and run the monitors push command again.

**Note on updates**:
* It's not possible to remove labels using this tool, you'll have to do that manually. Adding new labels is supported.
* Changing alert policy id is currently not supported.

## Build

```
mvn clean install
```
