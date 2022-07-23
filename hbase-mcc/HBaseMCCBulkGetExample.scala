package com.cloudera.hbase.mcc.scala.test

import com.cloudera.hbase.mcc.MultiClusterConf
import org.apache.hadoop.hbase.{CellUtil, TableName}
import org.apache.hadoop.hbase.client.{Get, Result}
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}


object HBaseMCCBulkGetExample {
  def main(args: Array[String]) {
    val tableName = "test_table"
    val sparkConf = new SparkConf().setAppName("HBaseMCCBulkGetExample Version 2 - Simplified config " + tableName)
    val sc = new SparkContext(sparkConf)

    val mccConf = new MultiClusterConf(args, sc)

    try {

      //[(Array[Byte])]
      val rdd = sc.parallelize(Array(
        Bytes.toBytes("1"),
        Bytes.toBytes("2"),
        Bytes.toBytes("3"),
        Bytes.toBytes("4"),
        Bytes.toBytes("5"),
        Bytes.toBytes("6"),
        Bytes.toBytes("7")))


      val hbaseContext = new HBaseContext(sc, mccConf.getConfiguration())

      val getRdd = hbaseContext.bulkGet[Array[Byte], String](
        TableName.valueOf(tableName),
        2,
        rdd,
        record => {
          System.out.println("making Get")
          new Get(record)
        },
        (result: Result) => {

          val it = result.listCells().iterator()
          val b = new StringBuilder

          b.append(Bytes.toString(result.getRow) + ":")

          while (it.hasNext) {
            val cell = it.next()
            val q = Bytes.toString(CellUtil.cloneQualifier(cell))
            if (q.equals("counter")) {
              b.append("(" + q + "," + Bytes.toLong(CellUtil.cloneValue(cell)) + ")")
            } else {
              b.append("(" + q + "," + Bytes.toString(CellUtil.cloneValue(cell)) + ")")
            }
          }
          b.toString()
        })

      getRdd.collect().foreach(v => println(v))

    } finally {
      sc.stop()
    }
  }
