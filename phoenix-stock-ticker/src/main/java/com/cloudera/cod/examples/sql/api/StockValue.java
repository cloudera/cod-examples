// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockValue {

  private float price;
  private long instant;

  public StockValue() {}

  public StockValue(float price, long millis) {
    this.price = price;
    this.instant = millis;
  }

  @JsonProperty
  public float getPrice() {
    return price;
  }

  @JsonProperty
  public void setPrice(float price) {
    this.price = price;
  }

  @JsonProperty
  public long getInstant() {
    return instant;
  }

  @JsonProperty
  public void setInstant(long instant) {
    this.instant = instant;
  }
}
