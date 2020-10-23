// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company extends CompanyInfo {

  private long id;

  public Company() {}

  public Company(long id, String tickerName, String companyName) {
    super(tickerName, companyName);
    this.id = id;
  }

  @JsonProperty
  public long getId() {
    return id;
  }
}
