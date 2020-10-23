// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Client extends AbstractSqlClient {
  public static final String TABLE_NAME = "COD_SQL_TEST";

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.Client \"jdbc:phoenix:host:2181:/hbase\"");
    }
    Client c = new Client();
    final String clusterKey = args[0].trim();
    try (Connection conn = DriverManager.getConnection(clusterKey)) {
      c.run(TABLE_NAME, conn);
    }
  }
}
