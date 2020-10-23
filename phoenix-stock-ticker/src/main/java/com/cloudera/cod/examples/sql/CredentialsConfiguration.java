// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class CredentialsConfiguration extends Configuration {
  private String username;
  private String password;

  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }
}
