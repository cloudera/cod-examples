package com.cloudera.hbase.mcc.scala.test

import com.cloudera.hbase.mcc.MultiClusterConf
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.spark.HBaseRDDFunctions._
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object HBaseMCCBulkPutExample {
  def main(args: Array[String]) {
    val tableName = "test_table"
    val columnFamily = "cf1"
    val sparkConf = new SparkConf().setAppName("HBaseBulkGetExample Version 2 - Simplified config " + tableName)
    val sc = new SparkContext(sparkConf)

    val mccConf = new MultiClusterConf(args, sc)

    val hbaseContext = new HBaseContext(sc, mccConf.getConfiguration())

    val rdd = sc.parallelize(Array(
      (Bytes.toBytes("1"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("1")))),
      (Bytes.toBytes("2"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("2")))),
      (Bytes.toBytes("3"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("3")))),
      (Bytes.toBytes("4"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("4")))),
      (Bytes.toBytes("5"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("5")))),
      (Bytes.toBytes("6"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("6")))),
      (Bytes.toBytes("7"), Array((Bytes.toBytes(columnFamily), Bytes.toBytes("1"), Bytes.toBytes("7"))))
    ))

    //put the RDD in Hbase
    rdd.hbaseBulkPut(hbaseContext, TableName.valueOf(tableName),
      (putRecord) => {
        val put = new Put(putRecord._1)
        putRecord._2.foreach((putValue) => put.addColumn(putValue._1, putValue._2,
          putValue._3))
        put
      })
    println("Completed")
    sc.stop()
  }
}
