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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.cod.examples.sql.StockAppConfiguration;
import com.cloudera.cod.examples.sql.api.GlobalQuote;
import com.cloudera.cod.examples.sql.api.GlobalQuoteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WriteAllTickerValuesCommand extends ConfiguredCommand<StockAppConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(WriteAllTickerValuesCommand.class);
  // Set by configure?
  private String apiKey = null;

  public WriteAllTickerValuesCommand() {
    super("write-ticker-values", "Reads all company info and stores current ticker value");
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);

    subparser.addArgument("-a", "--apiKey")
        .dest("apiKey")
        .type(String.class)
        .required(true)
        .help("API Key From AlphaVantage");
  }

  @Override
  protected void run(Bootstrap<StockAppConfiguration> bootstrap, Namespace namespace,
      StockAppConfiguration configuration) throws Exception {
    OkHttpClient client = new OkHttpClient();
    final String companyTableName = configuration.getDatabaseConfiguration().getTableName();
    final String valueTableName = configuration.getDatabaseConfiguration().getValueTableName();
    final Timestamp timestamp = Timestamp.from(Instant.now());

    configuration.createConnection();
    Connection conn = configuration.getConnection();
    try (Statement readStmt = conn.createStatement();
        PreparedStatement updateStmt = conn.prepareStatement("UPSERT INTO " + valueTableName
            + "(company_id, instant, price) VALUES(?, ?, ?)")) {

      ResultSet results = readStmt.executeQuery("SELECT id, tickerName FROM " + companyTableName);
      int numItems = 0;
      while (results.next()) {
        final int companyId = results.getInt(1);
        final String tickerName = results.getString(2);

        LOG.debug("Looking up {}", tickerName);
        GlobalQuote quote = getGlobalQuote(tickerName, apiKey, client);

        if (quote != null) {
          numItems++;
          LOG.debug("Writing price {}", quote.getPrice());
          updateStmt.setInt(1, companyId);
          updateStmt.setTimestamp(2, timestamp);
          updateStmt.setFloat(3, Float.parseFloat(quote.getPrice()));
          updateStmt.executeUpdate();
        } else {
          LOG.warn("Failed to get price for {}", tickerName);
        }
      }

      LOG.info("Wrote prices for {} companies", numItems);

      conn.commit();
    }
  }

  private String getUrl(String ticker, String apiKey) {
    // https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM&apikey=demo
    return "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
        + ticker + "&apikey=" + apiKey;
  }

  private GlobalQuote getGlobalQuote(String ticker, String apiKey, OkHttpClient client) throws Exception {
    Request request = new Request.Builder()
        .url(getUrl(ticker, apiKey))
        .build();

    for (int i = 0; i < 3; i++) {
      String json;
      try (Response response = client.newCall(request).execute()) {
        json = response.body().string();
        LOG.debug("Got response: {}", json);
      }

      if (json.contains("Thank you for using Alpha Vantage")) {
        LOG.debug("Waiting, and will retry");
        try {
          Thread.sleep(TimeUnit.MILLISECONDS.convert(65, TimeUnit.SECONDS));
          continue;
        } catch (InterruptedException e) {
          return null;
        }
      }

      final ObjectMapper mapper = Jackson.newObjectMapper();
      GlobalQuoteResponse gqr = mapper.readValue(json, GlobalQuoteResponse.class);
      return gqr.getGlobalQuote();
    }
    return null;
  }
}
