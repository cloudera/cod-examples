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

from common import *
import json
import requests
from requests.auth import HTTPBasicAuth

request = requests.get(baseurl + "/" + table_name + "/COD_NOSQL__REST_TEST-*", headers={"Accept": "application/json"},
                       auth=HTTPBasicAuth(DB_USER, DB_PASS))

if not is_successful(request):
    print("Could not get messages from HBase. Status code was:\n" + str(request.status_code))
    quit()

bleats = json.loads(request.text)

for row in bleats['Row']:
    message = ''
    timestamp = ''
    user = ''

    for cell in row['Cell']:
        column_name_info = base64.b64decode(cell['column'])
        column_name_info = str(column_name_info, 'utf8')
        value_info = base64.b64decode(cell['$'])
        value_info = str(value_info, 'utf8')
        if column_name_info == cf_name + ":" + message_column:
            message = value_info
        elif column_name_info == cf_name + ":" + created_time:
            timestamp = value_info
        elif column_name_info == cf_name + ":" + username:
            user = value_info
    print("A message" + "\"" + message + "\" from the user \"" + user + "\" created on " + timestamp)
