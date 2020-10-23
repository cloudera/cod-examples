// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cloudera.cod.examples.sql.StockAppConfiguration;
import com.cloudera.cod.examples.sql.api.Company;
import com.cloudera.cod.examples.sql.api.CompanyValue;
import com.cloudera.cod.examples.sql.views.CompanyValueView;
import com.codahale.metrics.annotation.Timed;

@Path("/value")
@Produces(MediaType.APPLICATION_JSON)
public class CompanyValueResource {
  private final StockAppConfiguration conf;
  private final Connection conn;

  public CompanyValueResource(StockAppConfiguration conf) {
    this.conf = conf;
    this.conn = conf.getConnection();
  }

  @GET
  @Timed
  @Path("/{id}")
  @Produces(MediaType.TEXT_HTML)
  public CompanyValueView renderCompanyValue(@PathParam("id") int companyId) throws SQLException {
    // Copy from CompanyResource
    try (PreparedStatement stmt = conn.prepareStatement("SELECT id, tickerName, companyName from "
            + conf.getDatabaseConfiguration().getTableName() + " where id = ?")) {
      stmt.setLong(1, companyId);
      ResultSet results = null;
      try {
        results = stmt.executeQuery();
        if (results.next()) {
          Company c = new Company(results.getLong(1), results.getString(2), results.getString(3));
          return new CompanyValueView(c, getValues(companyId));
        }
        return null;
      } finally {
        if (results != null) {
          results.close();
        }
      }
    }
  }
  
  @GET
  @Timed
  @Path("/{id}")
  public List<CompanyValue> getValues(@PathParam("id") int companyId) throws SQLException {
    return getValues(companyId, 10);
  }

  @GET
  @Timed
  @Path("/{id}/all")
  public List<CompanyValue> getAllValues(@PathParam("id") int companyId) throws SQLException {
    return getValues(companyId, Integer.MAX_VALUE);
  }

  List<CompanyValue> getValues(int companyId, int limit) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT company_id, price, instant FROM "
            + conf.getDatabaseConfiguration().getValueTableName() + " WHERE company_id = ? LIMIT ?")) {
      stmt.setInt(1, companyId);
      stmt.setInt(2, limit);
      try (ResultSet results = stmt.executeQuery()) {
        List<CompanyValue> values = new ArrayList<>();
        while (results.next()) {
          values.add(new CompanyValue(results.getInt(1), results.getFloat(2),
              results.getTimestamp(3).getTime()));
        }
        return values;
      }
    }
    
  }

  @POST
  @Timed
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void addValue(List<CompanyValue> values) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("UPSERT INTO " 
            + conf.getDatabaseConfiguration().getValueTableName()
            + "(company_id, instant, price) VALUES(?, ?, ?)")) {
      for (CompanyValue value : values) {
        stmt.setInt(1, value.getCompanyId());
        stmt.setTimestamp(2, Timestamp.from(Instant.ofEpochMilli(value.getInstant())));
        stmt.setFloat(3, value.getPrice());
        stmt.executeUpdate();
      }
      conn.commit();
    }
  }
}
