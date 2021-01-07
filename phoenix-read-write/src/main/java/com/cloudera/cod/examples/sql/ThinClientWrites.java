/*
 * Copyright 2021 Cloudera, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.cod.examples.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class ThinClientWrites extends AbstractSqlClient {
  public static final String TABLE_NAME = "COD_THIN_SQL_TEST";

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.ThinClient \"jdbc:phoenix:thin:url=http://localhost:8765;serialization=PROTOBUF\" 10000");
    }
    Client c = new Client();
    final String jdbcUrl = args[0].trim();
    final int numRecords = Integer.parseInt(args[1]);
    try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
      c.runWrites(TABLE_NAME, conn, numRecords);
    }
  }
}
