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
package com.cloudera.cod.examples.sql.cli;

import java.sql.Connection;
import java.sql.Statement;

import com.cloudera.cod.examples.sql.StockAppConfiguration;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class CreateTablesCommand extends ConfiguredCommand<StockAppConfiguration> {

  public CreateTablesCommand() {
    super("create-tables", "Creates the necessary tables");
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
  }

  @Override
  protected void run(Bootstrap<StockAppConfiguration> bootstrap, Namespace namespace,
      StockAppConfiguration configuration) throws Exception {
    final String tableName = configuration.getDatabaseConfiguration().getTableName();
    configuration.createConnection();
    Connection conn = configuration.getConnection();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("CREATE SEQUENCE IF NOT EXISTS " + configuration.getDatabaseConfiguration().getSequenceName() 
          + " START WITH 0 INCREMENT BY 1");
      // a company's stock ticker
      stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName
          + "(id integer not null primary key, tickerName varchar, companyName varchar)"
          + " SPLIT ON (2,3,4,5,6,7,8,9)"); 
      // Stores values for a stock at a point in time
      stmt.execute("CREATE TABLE IF NOT EXISTS " + configuration.getDatabaseConfiguration().getValueTableName()
          + "(company_id INTEGER not null, instant TIMESTAMP not null, price FLOAT not null, "
          + " CONSTRAINT pk PRIMARY KEY(company_id, instant, price)) SPLIT ON (2,3,4,5,6,7,8,9)");
    }
  }

}
