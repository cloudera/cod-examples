#!/usr/bin/env python3
from common import *
import json
import requests
from requests.auth import HTTPBasicAuth

request = requests.get(baseurl + "/" + table_name + "/COD_NOSQL__REST_TEST-*", headers={"Accept": "application/json"},
                       verify=False,
                       auth=HTTPBasicAuth(DB_USER, DB_PASS))

if not is_successful(request):
    print("Could not get messages from HBase. Status code was:\n" + str(request.status_code))
    quit()

bleats = json.loads(request.text)

for row in bleats['Row']:
    message = ''
    lineNumber = 0
    username = ''

    for cell in row['Cell']:
        tmp = cell['column']

        column_name_info = base64.b64decode(cell['column'])
        column_name_info = str(column_name_info, 'utf8')
        value_info = base64.b64decode(cell['$'])
        value_info = str(value_info, 'utf8')
        print("column_name_info: " + column_name_info + "; value: " + value_info)
