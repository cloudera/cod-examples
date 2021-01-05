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

public class DeleteTablesCommand extends ConfiguredCommand<StockAppConfiguration> {

  public DeleteTablesCommand() {
    super("delete-tables", "Deletes the tables");
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
  }

  @Override
  protected void run(Bootstrap<StockAppConfiguration> bootstrap, Namespace namespace,
      StockAppConfiguration configuration) throws Exception {
    final String tableName = configuration.getDatabaseConfiguration().getTableName();
    final String valueTableName = configuration.getDatabaseConfiguration().getValueTableName();
    configuration.createConnection();
    Connection conn = configuration.getConnection();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("DROP SEQUENCE IF EXISTS " + tableName + "_seq");
      stmt.execute("DROP TABLE IF EXISTS " + tableName); 
      stmt.execute("DROP TABLE IF EXISTS " + valueTableName);
    }
  }

}
