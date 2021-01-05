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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class DatabaseConfiguration extends Configuration {
  private String jdbcUrl;
  private String tableName = "companies";

  @JsonProperty
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  @JsonProperty
  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  @JsonProperty
  public String getTableName() {
    return tableName;
  }

  @JsonProperty
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getSequenceName() {
    return getTableName() + "_seq";
  }

  public String getValueTableName() {
    return getTableName() + "_value";
  }
}
