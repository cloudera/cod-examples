// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
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
