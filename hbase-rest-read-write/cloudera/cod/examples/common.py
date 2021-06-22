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

import base64
import os
import struct

# TODO: change baseurl to your own rest server url
baseurl = "https://cod-13j43am48zyyw-gateway0.cod-729.xcu2-8y8x.dev.cldr.work/cod-13j43am48zyyw/cdp-proxy-api/hbase"

DB_USER = os.environ.get("DB_USER")
DB_PASS = os.environ.get("DB_PASS")

table_name = "COD_NOSQL_REST_TEST"
cf_name = "info"
username = "username"

message_column = "message"
created_time = "create_time"
username_column = "username"


def is_successful(request):
    if 200 <= request.status_code <= 299:
        return True
    else:
        return False


# Method for encoding ints with base64 encoding
def encode(n):
    data = struct.pack("i", n)
    s = base64.b64encode(data)
    return s


# Method for decoding ints with base64 encoding
def decode(s):
    data = base64.b64decode(s)
    n = struct.unpack("i", data)
    return n[0]
