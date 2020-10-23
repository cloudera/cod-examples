// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyInfo {

  String tickerName;
  String companyName;

  public CompanyInfo() {}

  public CompanyInfo(String tickerName, String companyName) {
    this.tickerName = tickerName;
    this.companyName = companyName;
  }


  @JsonProperty
  public String getTickerName() {
    return tickerName;
  }

  @JsonProperty
  public String getCompanyName() {
    return companyName;
  }

}
