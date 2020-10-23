// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.views;

import java.util.List;

import com.cloudera.cod.examples.sql.api.Company;

import io.dropwizard.views.View;

public class CompaniesView extends View {
  private final List<Company> companies;

  public CompaniesView(List<Company> companies) {
    super("companies.ftl");
    this.companies = companies;
  }

  public List<Company> getCompanies() {
    return this.companies;
  }
}
