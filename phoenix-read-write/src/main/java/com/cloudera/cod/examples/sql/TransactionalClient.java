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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TransactionalClient {

  public static void main(String[] args) throws SQLException {
    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.TransactionalClient \"jdbc:phoenix:host:2181:/hbase\"");
    }
    TransactionalClient c = new TransactionalClient();
    final String clusterKey = args[0].trim();
    c.runTransactions(clusterKey);
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
   * @param orderId Order ID of the Creating Order.
   * @param customerId Customer ID of the customer made an order.
   * @param itemVsQuantity Items selected with quantities for order.
   * @throws SQLException
   */
  public void createOrder(Connection conn, String orderId, String customerId,
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
  }

  public void runTransactions(String jdbcUrl) throws SQLException {
    Connection conn = DriverManager.getConnection(jdbcUrl);
    createTransactionalTables(conn);
    conn.setAutoCommit(false);
    populateData(conn);
    // Create Multiple Connections for different terminal clients to make order entries.
    Connection firstTerminalConn = DriverManager.getConnection(jdbcUrl);
    firstTerminalConn.setAutoCommit(false);
    Connection secondTerminalConn = DriverManager.getConnection(jdbcUrl);
    secondTerminalConn.setAutoCommit(false);
    Connection thirdTerminalConn = DriverManager.getConnection(jdbcUrl);
    thirdTerminalConn.setAutoCommit(false);

    // Create Order OR001 with Items Book, Pen, Soap, Shampoo for customer John at first terminal
    // and committed.
    Map<String, Integer> firstTerminalItems = new HashMap<>();
    firstTerminalItems.put("ITM001", 2);
    firstTerminalItems.put("ITM002", 3);
    firstTerminalItems.put("ITM003", 2);
    createOrder(firstTerminalConn, "OR001", "CU001",
        firstTerminalItems);
    // commit order created at first terminal
    firstTerminalConn.commit();

    // Create Order OR002 with Items Phone, Charger for customer Angel at second terminal but
    // not committed.
    Map<String, Integer> secondTerminalItems = new HashMap<>();
    secondTerminalItems.put("ITM005", 1);
    secondTerminalItems.put("ITM006", 1);
    createOrder(secondTerminalConn, "OR002", "CU002",
        secondTerminalItems);

    // Create Order OR003 with Items Book, Pen, Phone, Charger for customer David at third terminal
    // and committed
    Map<String, Integer> thirdTerminalItems = new HashMap<>();
    thirdTerminalItems.put("ITM001", 1);
    thirdTerminalItems.put("ITM002", 1);
    thirdTerminalItems.put("ITM005", 1);
    thirdTerminalItems.put("ITM006", 1);
    createOrder(thirdTerminalConn, "OR003", "CU003",
        thirdTerminalItems);
    // commit order created at third terminal
    thirdTerminalConn.commit();

    // Since third terminal order committed and sharing the items in second terminal order
    // so committing the second terminal transactions aborts.
    try {
      secondTerminalConn.commit();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) from \"ORDER\"");
    rs.next();
    System.out.println("Number of orders " + rs.getInt(1));

    conn.close();
    firstTerminalConn.close();
    secondTerminalConn.close();
    thirdTerminalConn.close();
  }
}
