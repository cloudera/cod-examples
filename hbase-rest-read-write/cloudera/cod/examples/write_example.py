#!/usr/bin/env python3
from requests.auth import HTTPBasicAuth
from common import *
import json
import requests
from datetime import datetime
try:
    from UserDict import UserDict
    from UserDict import DictMixin
except ImportError:
    from collections import UserDict, OrderedDict
    from collections import MutableMapping as DictMixin

# change: change input for b64encode method from str to bytes
username_column_encoded = base64.b64encode(bytes(cf_name + ":" + username_column, 'utf-8'))
message_column_encoded = base64.b64encode(bytes(cf_name + ":" + message_column, 'utf-8'))
created_time_column_encoded = base64.b64encode(bytes(cf_name + ":" + created_time, 'utf-8'))

# Delete table if it exists
# Change: Add verify and auth part
request = requests.get(baseurl + "/" + table_name + "/schema", verify=False,
                       auth=HTTPBasicAuth(DB_USER, DB_PASS))

if is_successful(request):
    request = requests.delete(baseurl + "/" + table_name + "/schema", verify=False,
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

# Change: Add verify and auth part
request = requests.post(baseurl + "/" + table_name + "/schema", data=content,
                        headers={"Content-Type": "text/xml", "Accept": "text/xml"}, verify=False,
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
for i in range(0, 20):
    rowKey = username + "-" + str(i)
    rowKeyEncoded = base64.b64encode(bytes(rowKey, 'utf-8'))
    usernameEncoded = base64.b64encode(bytes(username + "-" + str(i), 'utf-8'))
    currentTimeEncoded = base64.b64encode(bytes(get_current_time(), 'utf-8'))
    testMessageEncoded = base64.b64encode(bytes("test message" + str(i), 'utf-8'))
    cell = OrderedDict([
        ("key", rowKeyEncoded.decode('utf-8')),
        ("Cell",
         [
             {"column": message_column_encoded.decode('utf-8'), "$": testMessageEncoded.decode('utf-8')},
             {"column": username_column_encoded.decode('utf-8'), "$": usernameEncoded.decode('utf-8')},
             {"column": created_time_column_encoded.decode('utf-8'), "$": currentTimeEncoded.decode('utf-8')},
         ])
    ])
    rows.append(cell)

request = requests.post(baseurl + "/" + table_name + "/" + rowKey, data=json.dumps(jsonOutput),
                        headers={"Content-Type": "application/json", "Accept": "application/json"}, verify=False,
                        auth=HTTPBasicAuth(DB_USER, DB_PASS))

if is_successful(request):
    print("Added messages for " + table_name)
else:
    print("Error out while loading data.  Status code was " + str(request.status_code) + "\n" + request.text)
    quit()
