// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.health;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.codahale.metrics.health.HealthCheck;

public class CompanyHealthCheck extends HealthCheck {
  private final String jdbcUrl;

  public CompanyHealthCheck(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @Override
  protected Result check() throws Exception {
    try (Connection conn = DriverManager.getConnection(jdbcUrl);
        Statement stmt = conn.createStatement()) {
      ResultSet results = null;
      try {
        results = stmt.executeQuery("SELECT 1 from SYSTEM.CATALOG LIMIT 1");
        if (results.next()) {
          if (1 == results.getInt(1)) {
            return Result.healthy();
          }
        }
        return Result.unhealthy("Not so good!");
      } finally {
        if (results != null) {
          results.close();
        }
      }
    }
  }
}
