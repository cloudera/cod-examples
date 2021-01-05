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
