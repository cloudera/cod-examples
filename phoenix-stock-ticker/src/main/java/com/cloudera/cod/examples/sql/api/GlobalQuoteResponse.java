// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobalQuoteResponse {

  private GlobalQuote resp;

  @JsonProperty("Global Quote")
  public GlobalQuote getGlobalQuote() {
    return resp;
  }

  @JsonProperty("Global Quote")
  public void setGlobalQuote(GlobalQuote resp) {
    this.resp = resp;
  }
}
