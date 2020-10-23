// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.sql.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cloudera.cod.examples.sql.StockAppConfiguration;
import com.cloudera.cod.examples.sql.api.Company;
import com.cloudera.cod.examples.sql.api.CompanyInfo;
import com.cloudera.cod.examples.sql.views.CompaniesView;
import com.cloudera.cod.examples.sql.views.CompanyView;
import com.codahale.metrics.annotation.Timed;

@Path("/")
public class CompanyResource {
  private final StockAppConfiguration conf;
  private final String tableName;
  private final String sequenceName;
  private final Connection conn;

  public CompanyResource(StockAppConfiguration conf) {
    this.conf = conf;
    this.tableName = conf.getDatabaseConfiguration().getTableName();
    this.sequenceName = conf.getDatabaseConfiguration().getSequenceName();
    this.conn = conf.getConnection();
  }

  @GET
  @Timed
  @Produces(MediaType.TEXT_HTML)
  public CompaniesView renderAllCompanies() throws SQLException {
    return new CompaniesView(getAllCompanies());
  }

  @GET
  @Path("/company/{id}")
  @Timed
  @Produces(MediaType.TEXT_HTML)
  public CompanyView renderCompany(@PathParam("id") int id) throws SQLException {
    Company company = getCompanyById(id);
    
    return new CompanyView(company);
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public List<Company> getNewestCompanies() throws SQLException {
    return getCompaniesWithLimit(20);
  }

  @GET
  @Timed
  @Path("/all")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Company> getAllCompanies() throws SQLException {
    return getCompaniesWithLimit(Integer.MAX_VALUE);
  }

  List<Company> getCompaniesWithLimit(int limit) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT id, tickerName, companyName from " + tableName
            + " LIMIT " + limit)) {
      ResultSet results = null;
      try {
        results = stmt.executeQuery();
        List<Company> companies = new ArrayList<>();
        while (results.next()) {
          companies.add(new Company(results.getLong(1), results.getString(2), results.getString(3)));
        }
        return companies;
      } finally {
        if (results != null) {
          results.close();
        }
      }
    }
  }

  @GET
  @Timed
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  public long getNumCompanies() throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT sum(1) from " + tableName)) {
      ResultSet results = null;
      try {
        results = stmt.executeQuery();
        return results.getLong(1);
      } finally {
        if (results != null) {
          results.close();
        }
      }
    }
  }

  @GET
  @Path("/company/{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Company getCompanyById(@PathParam("id") long id) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT id, tickerName, companyName from "
            + tableName + " where id = ?")) {
      stmt.setLong(1, id);
      ResultSet results = null;
      try {
        results = stmt.executeQuery();
        if (results.next()) {
          return new Company(results.getLong(1), results.getString(2), results.getString(3));
        }
        return null;
      } finally {
        if (results != null) {
          results.close();
        }
      }
    }
  }

  @POST
  @Timed
  @Path("/company/create")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public void createNewCompany(@FormParam("tickerName") String tickerName,
      @FormParam("companyName") String companyName) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("UPSERT INTO " +  tableName + "(id, tickerName, companyName)"
            + " values(NEXT VALUE FOR " + sequenceName + ", ?, ?)")) {
      stmt.setString(1, tickerName);
      stmt.setString(2, companyName);
      stmt.executeUpdate();
      conn.commit();
    }
  }

  @POST
  @Timed
  @Path("/company/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void createNewCompanyFromJson(List<CompanyInfo> companiesToCreate) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("UPSERT INTO " +  tableName + "(id, tickerName, companyName)"
            + " values(NEXT VALUE FOR " + sequenceName + ", ?, ?)")) {
      for (CompanyInfo info : companiesToCreate) {
        stmt.setString(1, info.getTickerName());
        stmt.setString(2, info.getCompanyName());
        stmt.executeUpdate();
      }
      conn.commit();
    }
  }
}
