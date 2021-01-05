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
