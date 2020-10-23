// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
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
