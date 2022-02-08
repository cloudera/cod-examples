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
package com.cloudera.cod.examples.spark

import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import java.sql.{DriverManager, SQLException}

object SparkApp {

  val tableName: String = "CODTRANSACTIONSTEST"

  def main(args: Array[String]) {

    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.SparkApp \"jdbc:phoenix:host:2181:/hbase\"");
    }


    val url = args(0)
    val zkUrl = args(0).split("jdbc:phoenix:")(1)
    val conn = DriverManager.getConnection(url)
    conn.setAutoCommit(true);
    var stmt = conn.createStatement();
    val sql = "DROP TABLE IF EXISTS " + tableName;
    stmt.execute(sql)
    stmt.execute("CREATE TABLE " + tableName +
      " (ID INTEGER PRIMARY KEY, COL1 VARCHAR, COL2 INTEGER) TRANSACTIONAL=true" +
      " SPLIT ON (200, 400, 600, 800, 1000)")
    val spark = SparkSession
      .builder()
      .appName("phoenix-test")
      .master("local")
      .getOrCreate()

    val schema = StructType(
      Seq(StructField("ID", IntegerType, nullable = false),
        StructField("COL1", StringType),
        StructField("COL2", IntegerType)))

    // Write rows from 1 to 500.
    var dataSet = List(Row(1, "1", 1), Row(2, "2", 2))
    for (w <- 3 to 500) {
      dataSet = dataSet :+ Row(w, "foo", w);
    }

    var rowRDD = spark.sparkContext.parallelize(dataSet)
    var df = spark.sqlContext.createDataFrame(rowRDD, schema)

    // Batch wise transactions:
    // ========================
    // Setting batch size to 100. For each batch of 100 records one transactions gets created.
    var extraOptions = "phoenix.transactions.enabled=true,phoenix.upsert.batch.size=100";
    df.write
      .format("phoenix")
      .options(Map("table" -> tableName, "zkUrl" -> zkUrl, "phoenixconfigs" -> extraOptions))
      .mode(SaveMode.Overwrite)
      .save()


    // Write rows from 500 to 1000.
    dataSet = List(Row(501, "500", 500), Row(502, "502", 502))
    for (w <- 503 to 1000) {
      dataSet = dataSet :+ Row(w, ""+w, w);
    }


    // Partition wise transactions:
    // ===========================
    // Setting batch size 0 means for partition one transaction gets created.
    rowRDD = spark.sparkContext.parallelize(dataSet)
    df = spark.sqlContext.createDataFrame(rowRDD, schema)
    extraOptions = "phoenix.transactions.enabled=true,phoenix.upsert.batch.size=0";

    df.write
      .format("phoenix")
      .options(Map("table" -> tableName, "zkUrl" -> zkUrl, "phoenixconfigs" -> extraOptions))
      .mode(SaveMode.Overwrite)
      .save()


    // Show casing conflict detection between streaming application and spark job.
    conn.setAutoCommit(false)
    stmt = conn.createStatement()
    // This will create transaction.
    // Value for COL1 in row 1001 is foo but in value writing through data frame is 1001
    // so which ever commits first gets priority and other update will be rolled back.
    stmt.execute("UPSERT INTO " + tableName + " VALUES(1001,'foo',1001)");
    dataSet = List(Row(1001, "1001", 1001), Row(1002, "1002", 1002))

    rowRDD = spark.sparkContext.parallelize(dataSet)
    df = spark.sqlContext.createDataFrame(rowRDD, schema)

    df.write
      .format("phoenix")
      .options(Map("table" -> tableName, "zkUrl" -> zkUrl, "phoenixconfigs" -> extraOptions))
      .mode(SaveMode.Overwrite)
      .save()
    try {
      conn.commit()
    } catch {
      case e: SQLException => {
        conn.rollback();
        println("This is expected because of trying to commit conflicting data set.")
      }
    }
    conn.close();
  }

}
