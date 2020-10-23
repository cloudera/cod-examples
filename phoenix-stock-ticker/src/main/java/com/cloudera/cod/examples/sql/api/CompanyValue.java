// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyValue extends StockValue {

  private int companyId;

  public CompanyValue() {
    super();
  }

  public CompanyValue(int companyId, float price, long millis) {
    super(price, millis);
    this.companyId = companyId;
  }

  @JsonProperty
  public int getCompanyId() {
    return companyId;
  }

  @JsonProperty
  public void setCompanyId(int companyId) {
    this.companyId = companyId;
  }
}
