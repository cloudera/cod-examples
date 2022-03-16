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
package com.cloudera.cod.examples.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public abstract class AbstractSqlClient {

  public void dropIfExists(String tableName, Statement stmt) throws SQLException {
    System.out.println("Dropping " + tableName);
    stmt.execute("DROP TABLE IF EXISTS " + tableName);
  }
  
  public void createTable(String tableName, Statement stmt, boolean ifNotExists) throws SQLException {
      System.out.println("Creating " + tableName);
      stmt.execute("CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + tableName + 
          " (pk integer not null primary key, data varchar)");
  }

  public int countRows(String tableName, Statement stmt) throws SQLException {
    try (ResultSet results = stmt.executeQuery("SELECT COUNT(1) FROM " + tableName)) {
      if (!results.next()) {
        throw new RuntimeException("Query should have results");
      }
      return results.getInt(1);
    }
  }

  public void run(String tableName, Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      dropIfExists(tableName, stmt);
  
      createTable(tableName, stmt, false);
      conn.setAutoCommit(false);
      try (PreparedStatement pstmt = conn.prepareStatement("UPSERT INTO " + tableName + " values(?,?)")) {
        System.out.println("Writing to " + tableName);
        for (int i = 0; i < 100; i++) {
          pstmt.setInt(1, i);
          pstmt.setString(2, Integer.toString(i));
          pstmt.executeUpdate();
        }
        conn.commit();
      }
  
      System.out.println("Found " + countRows(tableName, stmt) + " records from " + tableName);
    }
  }

  public void runWrites(String tableName, Connection conn, int numRecords) throws SQLException {
    final boolean prevAutoCommit = conn.getAutoCommit();
    try (Statement stmt = conn.createStatement();
        PreparedStatement pstmt = conn.prepareStatement("UPSERT INTO " + tableName + " values(?,?)")) {
      createTable(tableName, stmt, true);

      conn.setAutoCommit(false);
      int batchSize = 500;
      long start = System.nanoTime();
      for (int i = 0; i < numRecords; i++) {
        pstmt.setInt(1, i);
        pstmt.setString(2, Integer.toString(i));
        pstmt.addBatch();
        if (i % 500 == 0) {
          System.out.println(new Date() + " Flushing batched records");
          pstmt.executeBatch();
          conn.commit();
        }
      }
      pstmt.executeBatch();
      conn.commit();
      long end = System.nanoTime();
      long durationInMillis = TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS);
      System.out.println("Wrote " + numRecords + " records in " + durationInMillis + "ms");

      System.out.println("Read " + countRows(tableName, stmt) + " records from " + tableName);
    } finally {
      conn.setAutoCommit(prevAutoCommit);
    }
  }

  public void createTransactionalTables(Connection connection) throws SQLException {
    dropTransactionalTablesIfExists(connection);
    System.out.println("Creating transactional tables");
    Statement stmt = connection.createStatement();
    stmt.execute("CREATE TABLE IF NOT EXISTS ITEM " +
        " (id varchar not null primary key, name varchar, quantity integer) transactional=true");
    stmt.execute("CREATE TABLE IF NOT EXISTS CUSTOMER " +
        " (id varchar not null primary key, name varchar, address varchar) transactional=true");
    stmt.execute("CREATE TABLE IF NOT EXISTS  \"ORDER\" " +
        " (id varchar not null primary key, customer_id varchar, creation_time varchar) "
        + "transactional=true");
    stmt.execute("CREATE TABLE IF NOT EXISTS ORDER_LINE_ITEM " +
        " (order_id varchar not null, item_id varchar not null, sale_quantity integer, "
        + " constraint pk primary key(order_id, item_id)) transactional=true");
  }

  public void dropTransactionalTablesIfExists(Connection connection) throws SQLException {
    System.out.println("Dropping transactional tables");
    Statement stmt = connection.createStatement();
    stmt.execute("DROP TABLE IF EXISTS ITEM");
    stmt.execute("DROP TABLE IF EXISTS CUSTOMER");
    stmt.execute("DROP TABLE IF EXISTS \"ORDER\"");
    stmt.execute("DROP TABLE IF EXISTS ORDER_LINE_ITEM");
  }

  public void populateData(Connection connection) throws SQLException {
    System.out.println("Populate data to transactional tables");
    Statement stmt = connection.createStatement();
    populateItemData(connection);
    populateCustomerData(connection);
  }


  public void populateItemData(Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    stmt.execute("UPSERT INTO ITEM VALUES('ITM001','Book', 5)");
    stmt.execute("UPSERT INTO ITEM VALUES('ITM002','Pen', 5)");
    stmt.execute("UPSERT INTO ITEM VALUES('ITM003','Soap', 5)");
    stmt.execute("UPSERT INTO ITEM VALUES('ITM004','Shampoo', 5)");
    stmt.execute("UPSERT INTO ITEM VALUES('ITM005','Phone', 5)");
    stmt.execute("UPSERT INTO ITEM VALUES('ITM006','Charger', 5)");
    connection.commit();
  }

  public void populateCustomerData(Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    stmt.execute("UPSERT INTO CUSTOMER VALUES('CU001','John', 'foo')");
    stmt.execute("UPSERT INTO CUSTOMER VALUES('CU002','Angel', 'faa')");
    stmt.execute("UPSERT INTO CUSTOMER VALUES('CU003','David', 'soo')");
    stmt.execute("UPSERT INTO CUSTOMER VALUES('CU004','Robert', 'laa')");
    stmt.execute("UPSERT INTO CUSTOMER VALUES('CU005','James', 'naa')");
    connection.commit();
  }

  /**
   *
   * @param conn connection used for performing transactions.
   * @param commit whether to commit the order transaction.
   * @param orderId Order ID of the Creating Order.
   * @param customerId Customer ID of the customer made an order.
   * @param itemVsQuantity Items selected with quantities for order.
   * @throws SQLException
   */
  public void createOrder(Connection conn, boolean commit, String orderId, String customerId,
      Map<String, Integer> itemVsQuantity) throws SQLException {
    conn.setAutoCommit(false);
    Statement stmt = conn.createStatement();
    stmt.execute("UPSERT INTO \"ORDER\" VALUES('" + orderId + "','" + customerId + "',"
        + " CURRENT_DATE()||' '|| CURRENT_TIME())");
    for(Entry<String, Integer> item: itemVsQuantity.entrySet()) {
      String itemID = item.getKey();
      int saleQuantity = item.getValue();
      stmt.execute("UPSERT INTO ORDER_LINE_ITEM VALUES('"+ orderId+"','" +itemID+"',1)");
      stmt.execute("UPSERT INTO ITEM(ID, QUANTITY)"
          + " SELECT '"+itemID+"', QUANTITY - " + saleQuantity + " FROM ITEM "
          + " WHERE ID = '" + itemID + "'");
    }
    if(commit) {
      conn.commit();
    }
  }

  public void runTransactions(String jdbcUrl) throws SQLException {
    Connection conn = DriverManager.getConnection(jdbcUrl);
    createTransactionalTables(conn);
    conn.setAutoCommit(false);
    populateData(conn);
    // Create Multiple Connections for different terminal clients to make order entries.
    Connection firstTerminalConn = DriverManager.getConnection(jdbcUrl);
    Connection secondTerminalConn = DriverManager.getConnection(jdbcUrl);
    Connection thirdTerminalConn = DriverManager.getConnection(jdbcUrl);

    // Create Order OR001 with Items Book, Pen, Soap, Shampoo for customer John at first terminal
    // and committed.
    Map<String, Integer> firstTerminalItems = new HashMap<>();
    firstTerminalItems.put("ITM001", 2);
    firstTerminalItems.put("ITM002", 3);
    firstTerminalItems.put("ITM003", 2);
    createOrder(firstTerminalConn, true, "OR001", "CU001",
        firstTerminalItems);

    // Create Order OR002 with Items Phone, Charger for customer Angel at second terminal but
    // not committed.
    Map<String, Integer> secondTerminalItems = new HashMap<>();
    secondTerminalItems.put("ITM005", 1);
    secondTerminalItems.put("ITM006", 1);
    createOrder(secondTerminalConn, false, "OR002", "CU002",
        secondTerminalItems);

    // Create Order OR003 with Items Book, Pen, Phone, Charger for customer David at third terminal
    // and committed
    Map<String, Integer> thirdTerminalItems = new HashMap<>();
    thirdTerminalItems.put("ITM001", 1);
    thirdTerminalItems.put("ITM002", 1);
    thirdTerminalItems.put("ITM005", 1);
    thirdTerminalItems.put("ITM006", 1);
    createOrder(thirdTerminalConn, true, "OR003", "CU003",
        thirdTerminalItems);

    // Since third terminal order committed and sharing the items in second terminal order
    // so committing the second terminal transactions aborts.
    try {
      secondTerminalConn.commit();
    } catch (SQLException e) {
      System.out.println("Aborting the transaction is expected because of data set conflicts"
          + " with other transactions");
      e.printStackTrace();
    }

    conn.setAutoCommit(true);
    ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) from \"ORDER\"");
    rs.next();
    System.out.println("Number of orders " + rs.getInt(1));

    conn.close();
    firstTerminalConn.close();
    secondTerminalConn.close();
    thirdTerminalConn.close();
  }
}
