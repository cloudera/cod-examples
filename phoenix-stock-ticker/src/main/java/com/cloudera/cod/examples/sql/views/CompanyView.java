// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.views;

import java.util.List;

import com.cloudera.cod.examples.sql.api.Company;
import com.cloudera.cod.examples.sql.api.CompanyValue;

import io.dropwizard.views.View;

public class CompanyView extends View {
  private final Company company;

  public CompanyView(Company company) {
    super("company.ftl");
    this.company = company;
  }

  public Company getCompany() {
    return this.company;
  }
}
