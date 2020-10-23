// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
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
