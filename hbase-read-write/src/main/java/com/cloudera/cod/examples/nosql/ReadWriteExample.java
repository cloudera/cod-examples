// Copyright (c) 2020 Cloudera, Inc. All rights reserved.
package com.cloudera.cod.examples.nosql;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;

public class ReadWriteExample {
  public static final TableName COD_TABLE_NAME = TableName.valueOf("COD_NOSQL_TEST");
  public static final byte[] FAM = Bytes.toBytes("f1");
  public static final byte[] QUAL = Bytes.toBytes("q");

  public static void main(String[] args) throws Exception {
    Configuration conf = HBaseConfiguration.create();
    try (Connection conn = ConnectionFactory.createConnection(conf); Admin admin = conn.getAdmin()) {

      if (admin.tableExists(COD_TABLE_NAME)) {
        if (admin.isTableEnabled(COD_TABLE_NAME)) {
          System.out.println("Disabling " + COD_TABLE_NAME);
          admin.disableTable(COD_TABLE_NAME);
        }
        System.out.println("Deleting " + COD_TABLE_NAME);
        admin.deleteTable(COD_TABLE_NAME);
      }

      System.out.println("Creating " + COD_TABLE_NAME);
      admin.createTable(TableDescriptorBuilder.newBuilder(COD_TABLE_NAME)
          .setColumnFamily(ColumnFamilyDescriptorBuilder.of(FAM)).build());

      try (Table table = conn.getTable(COD_TABLE_NAME)) {
        System.out.println("Writing data to " + COD_TABLE_NAME);
        List<Put> puts = IntStream.range(0, 100)
            .mapToObj(i -> new Put(Bytes.toBytes("row" + i)).addColumn(FAM, QUAL, Bytes.toBytes("value" + i)))
            .collect(Collectors.toList());

        table.put(puts);

        System.out.println("Reading from " + COD_TABLE_NAME);
        System.out.println(table.get(new Get(Bytes.toBytes("row99"))));
      }
    }
  }
}
