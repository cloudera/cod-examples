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
