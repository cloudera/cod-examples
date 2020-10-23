// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlobalQuote {
  private String symbol;
  private String open;
  private String high;
  private String low;
  private String price;
  private String volume;
  private String latestTradingDay;
  private String previousClose;
  private String change;
  private String percentChange;

  @JsonProperty("01. symbol")
  public String getSymbol() {
    return symbol;
  }

  @JsonProperty("01. symbol")
  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @JsonProperty("02. open")
  public String getOpen() {
    return open;
  }

  @JsonProperty("02. open")
  public void setOpen(String open) {
    this.open = open;
  }

  @JsonProperty("03. high")
  public String getHigh() {
    return high;
  }

  @JsonProperty("03. high")
  public void setHigh(String high) {
    this.high = high;
  }

  @JsonProperty("04. low")
  public String getLow() {
    return low;
  }

  @JsonProperty("04. low")
  public void setLow(String low) {
    this.low = low;
  }
 
  @JsonProperty("05. price")
  public String getPrice() {
    return price;
  }

  @JsonProperty("05. price")
  public void setPrice(String price) {
    this.price = price;
  }

  @JsonProperty("06. volume")
  public String getVolume() {
    return volume;
  }

  @JsonProperty("06. volume")
  public void setVolume(String volume) {
    this.volume = volume;
  }

  @JsonProperty("07. latest trading day")
  public String getLatestTradingDay() {
    return latestTradingDay;
  }

  @JsonProperty("07. latest trading day")
  public void setLatestTradingDay(String latestTradingDay) {
    this.latestTradingDay = latestTradingDay;
  }

  @JsonProperty("08. previous close")
  public String getPreviousClose() {
    return previousClose;
  }

  @JsonProperty("08. previous close")
  public void setPreviousClose(String previousClose) {
    this.previousClose = previousClose;
  }

  @JsonProperty("09. change")
  public String getChange() {
    return change;
  }

  @JsonProperty("09. change")
  public void setChange(String change) {
    this.change = change;
  }

  @JsonProperty("10. change percent")
  public String getPercentChange() {
    return percentChange;
  }

  @JsonProperty("10. change percent")
  public void setPercentChange(String percentChange) {
    this.percentChange = percentChange;
  }
}
