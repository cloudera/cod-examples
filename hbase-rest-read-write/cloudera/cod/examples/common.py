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
