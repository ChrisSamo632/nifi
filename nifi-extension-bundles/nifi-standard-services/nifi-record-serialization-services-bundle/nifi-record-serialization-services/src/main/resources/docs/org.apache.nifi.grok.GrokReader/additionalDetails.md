<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# GrokReader

The GrokReader Controller Service provides a means for parsing and structuring input that is made up of unstructured
text, such as log files. Grok allows users to add a naming construct to Regular Expressions such that they can be
composed in order to create expressions that are easier to manage and work with. This Controller Service consists of one
Required Property and a few Optional Properties. The is named `Grok Pattern File` property specifies the filename of a
file that contains Grok Patterns that can be used for parsing log data. If not specified, a default patterns file will
be used. Its contents are provided below. There are also properties for specifying the schema to use when parsing data.
The schema is not required. However, when data is parsed a Record is created that contains all the fields present in the
Grok Expression (explained below), and all fields are of type String. If a schema is chosen, the field can be declared
to be a different, compatible type, such as number. Additionally, if the schema does not contain one of the fields in
the parsed data, that field will be ignored. This can be used to filter out fields that are not of interest.

Note: a `_raw` field is also added to preserve the original message.

The Required Property is named `Grok Expression` and specifies how to parse each incoming record. This is done by
providing a Grok Expression such as:
`%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} \[%{DATA:thread}\] %{DATA:class} %{GREEDYDATA:message}`. This
Expression will parse Apache NiFi log messages. This is accomplished by specifying that a line begins with the
`TIMESTAMP_ISO8601` pattern (which is a Regular Expression defined in the default Grok Patterns File). The value that
matches this pattern is then given the name `timestamp`. As a result, the value that matches this pattern will be
assigned to a field named `timestamp` in the Record that produced by this Controller Service.

## No Match Behavior

If a line is encountered in the FlowFile that does not match the configured Grok Expression, it is assumed that the line
is part of the previous message. If the line is the start of a stack trace, then the entire stack trace is read in and
assigned to a field named `STACK_TRACE`. Otherwise, the line will be processed according to the value defined in the "No
Match Behavior" property.

### Append to Previous Message

The line is appended to the last field defined in the Grok Expression. This is typically done because the last field is
a 'message' type of field, which can consist of new-lines.

### Skip Line

The line is completely dismissed.

### Raw Line

The fields will be null except the `_raw` field that will contain the line allowing further processing downstream.

## Examples

Assuming two messages (`<6>Feb 28 12:00:00 192.168.0.1 aliyun[11111]: [error] Syslog test` and
`This is a bad message...`) with the Grok Expression
`<%{POSINT:priority}>%{SYSLOGTIMESTAMP:timestamp} %{SYSLOGHOST:hostname} %{WORD:ident}%{GREEDYDATA:message}` ; and
assuming a JSON Writer, the following results will be generated:

### Append to Previous Message

```json
[
  {
    "priority": "6",
    "timestamp": "Feb 28 12:00:00",
    "hostname": "192.168.0.1",
    "ident": "aliyun",
    "message": "[11111]: [error] Syslog test\nThis is a bad message...",
    "stackTrace": null,
    "_raw": "<6>Feb 28 12:00:00 192.168.0.1 aliyun[11111]: [error] Syslog test\nThis is a bad message..."
  }
]

```

### Skip Line

```json
[
  {
    "priority": "6",
    "timestamp": "Feb 28 12:00:00",
    "hostname": "192.168.0.1",
    "ident": "aliyun",
    "message": "[11111]: [error] Syslog test",
    "stackTrace": null,
    "_raw": "<6>Feb 28 12:00:00 192.168.0.1 aliyun[11111]: [error] Syslog test"
  }
]

```

### Raw Line

```json
[
  {
    "priority": "6",
    "timestamp": "Feb 28 12:00:00",
    "hostname": "192.168.0.1",
    "ident": "aliyun",
    "message": "[11111]: [error] Syslog test",
    "stackTrace": null,
    "_raw": "<6>Feb 28 12:00:00 192.168.0.1 aliyun[11111]: [error] Syslog test"
  },
  {
    "priority": null,
    "timestamp": null,
    "hostname": null,
    "ident": null,
    "message": null,
    "stackTrace": null,
    "_raw": "This is a bad message..."
  }
]

```

## Schemas and Type Coercion

When a record is parsed from incoming data, it is separated into fields. Each of these fields is then looked up against
the configured schema (by field name) in order to determine what the type of the data should be. If the field is not
present in the schema, that field is omitted from the Record. If the field is found in the schema, the data type of the
received data is compared against the data type specified in the schema. If the types match, the value of that field is
used as-is. If the schema indicates that the field should be of a different type, then the Controller Service will
attempt to coerce the data into the type specified by the schema. If the field cannot be coerced into the specified
type, an Exception will be thrown.

The following rules apply when attempting to coerce a field value from one data type to another:

* Any data type can be coerced into a String type.
* Any numeric data type (Byte, Short, Int, Long, Float, Double) can be coerced into any other numeric data type.
* Any numeric value can be coerced into a Date, Time, or Timestamp type, by assuming that the Long value is the number
  of milliseconds since epoch (Midnight GMT, January 1, 1970).
* A String value can be coerced into a Date, Time, or Timestamp type, if its format matches the configured "Date
  Format," "Time Format," or "Timestamp Format."
* A String value can be coerced into a numeric value if the value is of the appropriate type. For example, the String
  value `8` can be coerced into any numeric type. However, the String value `8.2` can be coerced into a Double or Float
  type but not an Integer.
* A String value of "true" or "false" (regardless of case) can be coerced into a Boolean value.
* A String value that is not empty can be coerced into a Char type. If the String contains more than 1 character, the
  first character is used and the rest of the characters are ignored.
* Any "date/time" type (Date, Time, Timestamp) can be coerced into any other "date/time" type.
* Any "date/time" type can be coerced into a Long type, representing the number of milliseconds since epoch (Midnight
  GMT, January 1, 1970).
* Any "date/time" type can be coerced into a String. The format of the String is whatever DateFormat is configured for
  the corresponding property (Date Format, Time Format, Timestamp Format property).

If none of the above rules apply when attempting to coerce a value from one data type to another, the coercion will fail
and an Exception will be thrown.

## Examples

As an example, consider that this Controller Service is configured with the following properties:

| Property Name   | Property Value                                                                                            |
|-----------------|-----------------------------------------------------------------------------------------------------------|
| Grok Expression | `%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} \[%{DATA:thread}\] %{DATA:class} %{GREEDYDATA:message}` |

Additionally, let's consider a FlowFile whose contents consists of the following:

```
2016-08-04 13:26:32,473 INFO [Leader Election Notification Thread-1] o.a.n.c.l.e.CuratorLeaderElectionManager org.apache.nifi.controller.leader.election.CuratorLeaderElectionManager$ElectionListener@1fa27ea5 has been interrupted; no longer leader for role 'Cluster Coordinator'
2016-08-04 13:26:32,474 ERROR [Leader Election Notification Thread-2] o.apache.nifi.controller.FlowController One
Two
Three
org.apache.nifi.exception.UnitTestException: Testing to ensure we are able to capture stack traces
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_45]
	at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:308) [na:1.8.0_45]
        at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:180) [na:1.8.0_45]
        at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:294) [na:1.8.0_45]
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_45]
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_45]
        at java.lang.Thread.run(Thread.java:745) [na:1.8.0_45]
Caused by: org.apache.nifi.exception.UnitTestException: Testing to ensure we are able to capture stack traces
    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)
    ... 12 common frames omitted
2016-08-04 13:26:35,475 WARN [Curator-Framework-0] org.apache.curator.ConnectionState Connection attempt unsuccessful after 3008 (greater than max timeout of 3000). Resetting connection and trying again with a new connection.
```

In this case, the result will be that this FlowFile consists of 3 different records. The first record will contain the
following values:

| Field Name   | Field Value                                                                                                                                                              |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| timestamp    | 2016-08-04 13:26:32,473                                                                                                                                                  |
| level        | INFO                                                                                                                                                                     |
| thread       | Leader Election Notification Thread-1                                                                                                                                    |
| class        | o.a.n.c.l.e.CuratorLeaderElectionManager                                                                                                                                 |
| message      | org.apache.nifi.controller.leader.election.CuratorLeaderElectionManager/$ElectionListener@1fa27ea5 has been interrupted; no longer leader for role 'Cluster Coordinator' |
| STACK\_TRACE | _null_                                                                                                                                                                   |

The second record will contain the following values:

| Field Name   | Field Value                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| timestamp    | 2016-08-04 13:26:32,474                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| level        | ERROR                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| thread       | Leader Election Notification Thread-2                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| class        | o.apache.nifi.controller.FlowController                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| message      | One  <br>Two  <br>Three                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| STACK\_TRACE | org.apache.nifi.exception.UnitTestException: Testing to ensure we are able to capture stack traces<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>        at java.util.concurrent.Executors\$RunnableAdapter.call(Executors.java:511) \[na:1.8.0\_45\]<br>	at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:308) \[na:1.8.0\_45\]<br>        at java.util.concurrent.ScheduledThreadPoolExecutor\$ScheduledFutureTask.access/$301(ScheduledThreadPoolExecutor.java:180) \[na:1.8.0\_45\]<br>        at java.util.concurrent.ScheduledThreadPoolExecutor/$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:294) \[na:1.8.0\_45\]<br>        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) \[na:1.8.0\_45\]<br>        at java.util.concurrent.ThreadPoolExecutor/$Worker.run(ThreadPoolExecutor.java:617) \[na:1.8.0\_45\]<br>        at java.lang.Thread.run(Thread.java:745) \[na:1.8.0\_45\]<br>Caused by: org.apache.nifi.exception.UnitTestException: Testing to ensure we are able to capture stack traces<br>    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>    at org.apache.nifi.cluster.coordination.node.NodeClusterCoordinator.getElectedActiveCoordinatorAddress(NodeClusterCoordinator.java:185)<br>    ... 12 common frames omitted |

The third record will contain the following values:

| Field Name   | Field Value                                                                                                                                 |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| timestamp    | 2016-08-04 13:26:35,475                                                                                                                     |
| level        | WARN                                                                                                                                        |
| thread       | Curator-Framework-0                                                                                                                         |
| class        | org.apache.curator.ConnectionState                                                                                                          |
| message      | Connection attempt unsuccessful after 3008 (greater than max timeout of 3000). Resetting connection and trying again with a new connection. |
| STACK\_TRACE | _null_                                                                                                                                      |

## Default Patterns

The following patterns are available in the default Grok Pattern File:

```
# Log Levels
LOGLEVEL ([Aa]lert|ALERT|[Tt]race|TRACE|[Dd]ebug|DEBUG|[Nn]otice|NOTICE|[Ii]nfo|INFO|[Ww]arn?(?:ing)?|WARN?(?:ING)?|[Ee]rr?(?:or)?|ERR?(?:OR)?|[Cc]rit?(?:ical)?|CRIT?(?:ICAL)?|[Ff]atal|FATAL|[Ss]evere|SEVERE|EMERG(?:ENCY)?|[Ee]merg(?:ency)?)|FINE|FINER|FINEST|CONFIG

# Syslog Dates: Month Day HH:MM:SS
SYSLOGTIMESTAMP %{MONTH} +%{MONTHDAY} %{TIME}
PROG (?:[\w._/%-]+)
SYSLOGPROG %{PROG:program}(?:\[%{POSINT:pid}\])?
SYSLOGHOST %{IPORHOST}
SYSLOGFACILITY <%{NONNEGINT:facility}.%{NONNEGINT:priority}>
HTTPDATE %{MONTHDAY}/%{MONTH}/%{YEAR}:%{TIME} %{INT}

# Months: January, Feb, 3, 03, 12, December
MONTH \b(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\b
MONTHNUM (?:0?[1-9]|1[0-2])
MONTHNUM2 (?:0[1-9]|1[0-2])
MONTHDAY (?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])

# Days: Monday, Tue, Thu, etc...
DAY (?:Mon(?:day)?|Tue(?:sday)?|Wed(?:nesday)?|Thu(?:rsday)?|Fri(?:day)?|Sat(?:urday)?|Sun(?:day)?)

# Years?
YEAR (?>\d\d){1,2}
HOUR (?:2[0123]|[01]?[0-9])
MINUTE (?:[0-5][0-9])
# '60' is a leap second in most time standards and thus is valid.
SECOND (?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?)
TIME (?!<[0-9])%{HOUR}:%{MINUTE}(?::%{SECOND})(?![0-9])

# datestamp is YYYY/MM/DD-HH:MM:SS.UUUU (or something like it)
DATE_US_MONTH_DAY_YEAR %{MONTHNUM}[/-]%{MONTHDAY}[/-]%{YEAR}
DATE_US_YEAR_MONTH_DAY %{YEAR}[/-]%{MONTHNUM}[/-]%{MONTHDAY}
DATE_US %{DATE_US_MONTH_DAY_YEAR}|%{DATE_US_YEAR_MONTH_DAY}
DATE_EU %{MONTHDAY}[./-]%{MONTHNUM}[./-]%{YEAR}
ISO8601_TIMEZONE (?:Z|[+-]%{HOUR}(?::?%{MINUTE}))
ISO8601_SECOND (?:%{SECOND}|60)
TIMESTAMP_ISO8601 %{YEAR}-%{MONTHNUM}-%{MONTHDAY}[T ]%{HOUR}:?%{MINUTE}(?::?%{SECOND})?%{ISO8601_TIMEZONE}?
DATE %{DATE_US}|%{DATE_EU}
DATESTAMP %{DATE}[- ]%{TIME}
TZ (?:[PMCE][SD]T|UTC)
DATESTAMP_RFC822 %{DAY} %{MONTH} %{MONTHDAY} %{YEAR} %{TIME} %{TZ}
DATESTAMP_RFC2822 %{DAY}, %{MONTHDAY} %{MONTH} %{YEAR} %{TIME} %{ISO8601_TIMEZONE}
DATESTAMP_OTHER %{DAY} %{MONTH} %{MONTHDAY} %{TIME} %{TZ} %{YEAR}
DATESTAMP_EVENTLOG %{YEAR}%{MONTHNUM2}%{MONTHDAY}%{HOUR}%{MINUTE}%{SECOND}


POSINT \b(?:[1-9][0-9]*)\b
NONNEGINT \b(?:[0-9]+)\b
WORD \b\w+\b
NOTSPACE \S+
SPACE \s*
DATA .*?
GREEDYDATA .*
QUOTEDSTRING (?>(?<!\\)(?>"(?>\\.|[^\\"]+)+"|""|(?>'(?>\\.|[^\\']+)+')|''|(?>`(?>\\.|[^\\`]+)+`)|``))
UUID [A-Fa-f0-9]{8}-(?:[A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}

USERNAME [a-zA-Z0-9._-]+
USER %{USERNAME}
INT (?:[+-]?(?:[0-9]+))
BASE10NUM (?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\.[0-9]+)?)|(?:\.[0-9]+)))
NUMBER (?:%{BASE10NUM})
BASE16NUM (?<![0-9A-Fa-f])(?:[+-]?(?:0x)?(?:[0-9A-Fa-f]+))
BASE16FLOAT \b(?<![0-9A-Fa-f.])(?:[+-]?(?:0x)?(?:(?:[0-9A-Fa-f]+(?:\.[0-9A-Fa-f]*)?)|(?:\.[0-9A-Fa-f]+)))\b

# Networking
MAC (?:%{CISCOMAC}|%{WINDOWSMAC}|%{COMMONMAC})
CISCOMAC (?:(?:[A-Fa-f0-9]{4}\.){2}[A-Fa-f0-9]{4})
WINDOWSMAC (?:(?:[A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2})
COMMONMAC (?:(?:[A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2})
IPV6 ((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?
IPV4 (?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9])
IP (?:%{IPV6}|%{IPV4})
HOSTNAME \b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\.?|\b)
HOST %{HOSTNAME}
IPORHOST (?:%{HOSTNAME}|%{IP})
HOSTPORT %{IPORHOST}:%{POSINT}

# paths
PATH (?:%{UNIXPATH}|%{WINPATH})
UNIXPATH (?>/(?>[\w_%!$@:.,-]+|\\.)*)+
TTY (?:/dev/(pts|tty([pq])?)(\w+)?/?(?:[0-9]+))
WINPATH (?>[A-Za-z]+:|\\)(?:\\[^\\?*]*)+
URIPROTO [A-Za-z]+(\+[A-Za-z+]+)?
URIHOST %{IPORHOST}(?::%{POSINT:port})?
# uripath comes loosely from RFC1738, but mostly from what Firefox
# doesn't turn into %XX
URIPATH (?:/[A-Za-z0-9$.+!*'(){},~:;=@#%_\-]*)+
#URIPARAM \?(?:[A-Za-z0-9]+(?:=(?:[^&]*))?(?:&(?:[A-Za-z0-9]+(?:=(?:[^&]*))?)?)*)?
URIPARAM \?[A-Za-z0-9$.+!*'|(){},~@#%&/=:;_?\-\[\]]*
URIPATHPARAM %{URIPATH}(?:%{URIPARAM})?
URI %{URIPROTO}://(?:%{USER}(?::[^@]*)?@)?(?:%{URIHOST})?(?:%{URIPATHPARAM})?

# Shortcuts
QS %{QUOTEDSTRING}

# Log formats
SYSLOGBASE %{SYSLOGTIMESTAMP:timestamp} (?:%{SYSLOGFACILITY} )?%{SYSLOGHOST:logsource} %{SYSLOGPROG}:
COMMONAPACHELOG %{IPORHOST:clientip} %{USER:ident} %{USER:auth} \[%{HTTPDATE:timestamp}\] "(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})" %{NUMBER:response} (?:%{NUMBER:bytes}|-)
COMBINEDAPACHELOG %{COMMONAPACHELOG} %{QS:referrer} %{QS:agent}
```