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

# GetMongo

## Description:

This processor runs queries against a MongoDB instance or cluster and writes the results to a flowfile. It allows input,
but can run standalone as well.

## Specifying the Query

The query can be specified in one of three ways:

* Query configuration property.
* Query Attribute configuration property.
* FlowFile content.

If a value is specified in either of the configuration properties, it will not look in the FlowFile content for a query.

## Limiting/Shaping Results

The following options for limiting/shaping results are available:

* Limit - limit the number of results. This should not be confused with the "batch size" option which is a setting for
  the underlying MongoDB driver to tell it how many items to retrieve in each poll of the server.
* Sort - sort the result set. Requires a JSON document like _{ "someDate": -1 }_
* Projection - control which fields to return. Exampe, which would remove _\_id_: _{ "\_id": 0 }_

## Misc Options

Results Per FlowFile, if set, creates a JSON array out of a batch of results and writes the result to the output. Pretty
Print, if enabled, will format the JSON data to be easy read by a human (ex. proper indentation of fields).