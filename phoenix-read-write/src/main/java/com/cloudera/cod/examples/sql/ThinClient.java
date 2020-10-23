// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class ThinClient extends AbstractSqlClient {
  public static final String TABLE_NAME = "COD_THIN_SQL_TEST";

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.ThinClient \"jdbc:phoenix:thin:url=http://localhost:8765;serialization=PROTOBUF\"");
    }
    Client c = new Client();
    final String jdbcUrl = args[0].trim();
    try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
      c.run(TABLE_NAME, conn);
    }
  }

}
