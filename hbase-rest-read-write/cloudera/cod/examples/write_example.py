#!/usr/bin/env python3
# Copyright 2021 Cloudera, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.from setuptools import setup

from requests.auth import HTTPBasicAuth
from common import *
import json
import requests
from datetime import datetime
from collections import OrderedDict

username_column_encoded = base64.b64encode(bytes(cf_name + ":" + username_column, 'utf-8'))
message_column_encoded = base64.b64encode(bytes(cf_name + ":" + message_column, 'utf-8'))
created_time_column_encoded = base64.b64encode(bytes(cf_name + ":" + created_time, 'utf-8'))

# Delete table if it exists
request = requests.get(baseurl + "/" + table_name + "/schema",
                       auth=HTTPBasicAuth(DB_USER, DB_PASS))

if is_successful(request):
    request = requests.delete(baseurl + "/" + table_name + "/schema",
                              auth=HTTPBasicAuth(DB_USER, DB_PASS))

    if is_successful(request):
        print("Deleted table " + table_name)
    else:
        print("Error out.  Status code was " + str(request.status_code) + "\n" + request.text)

# Create Table
content = '<?xml version="1.0" encoding="UTF-8"?>'
content += '<TableSchema name="' + table_name + '">'
content += '  <ColumnSchema name="' + cf_name + '" />'
content += '</TableSchema>'

request = requests.post(baseurl + "/" + table_name + "/schema", data=content,
                        headers={"Content-Type": "text/xml", "Accept": "text/xml"},
                        auth=HTTPBasicAuth(DB_USER, DB_PASS))

if is_successful(request):
    print("Created table " + table_name)
else:
    print("Error out while creating table.  Status code was " + str(request.status_code) + "\n" + request.text)
    quit()


def get_current_time():
    now = datetime.now()  # current date and time
    date_time = now.strftime("%m/%d/%Y, %H:%M:%S")
    return date_time


rows = []
jsonOutput = {"Row": rows}
print("Writing data to " + table_name)
for i in range(0, 20):
    rowKey = username + "-" + str(i)
    rowKeyEncoded = base64.b64encode(bytes(rowKey, 'utf-8'))
    usernameEncoded = base64.b64encode(bytes(username + "-" + str(i), 'utf-8'))
    currentTime = get_current_time()
    currentTimeEncoded = base64.b64encode(bytes(currentTime, 'utf-8'))
    testMessage = "test message" + str(i)
    testMessageEncoded = base64.b64encode(bytes(testMessage, 'utf-8'))
    cell = OrderedDict([
        ("key", rowKeyEncoded.decode('utf-8')),
        ("Cell",
         [
             {"column": message_column_encoded.decode('utf-8'), "$": testMessageEncoded.decode('utf-8')},
             {"column": username_column_encoded.decode('utf-8'), "$": usernameEncoded.decode('utf-8')},
             {"column": created_time_column_encoded.decode('utf-8'), "$": currentTimeEncoded.decode('utf-8')},
         ])
    ])
    print("Row key: " + rowKey + "; Username: " +
          rowKey + "; " + "Message: " + testMessage + "; Created time: " + currentTime)
    rows.append(cell)


request = requests.post(baseurl + "/" + table_name + "/" + rowKey, data=json.dumps(jsonOutput),
                        headers={"Content-Type": "application/json", "Accept": "application/json"},
                        auth=HTTPBasicAuth(DB_USER, DB_PASS))

if is_successful(request):
    print("Successfully added messages for " + table_name)
else:
    print("Error out while loading data.  Status code was " + str(request.status_code) + "\n" + request.text)
    quit()
