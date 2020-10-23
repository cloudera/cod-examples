// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.views;

import java.util.List;

import com.cloudera.cod.examples.sql.api.Company;
import com.cloudera.cod.examples.sql.api.CompanyValue;

import io.dropwizard.views.View;

public class CompanyValueView extends View {
  private final Company company;
  private final List<CompanyValue> values;

  public CompanyValueView(Company company, List<CompanyValue> values) {
    super("companyvalue.ftl");
    this.company = company;
    this.values = values;
  }

  public Company getCompany() {
    return this.company;
  }

  public List<CompanyValue> getValues() {
    return values;
  }
}
