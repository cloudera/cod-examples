package com.cloudera.hbase.mcc.scala.test

import com.cloudera.hbase.mcc.MultiClusterConf
import org.apache.hadoop.fs.CommonConfigurationKeysPublic
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.spark.sql.functions.{col, concat, lit, when}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{Column, Dataset, SparkSession}
import org.slf4j.LoggerFactory

object StructuredStreamingExampleKafkaToHBase {
  val logger = LoggerFactory.getLogger(this.getClass.getName)
  case class HBaseData(rowKey: String, rowValue: String)
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Structured Streaming Kafka to HBase")
      .getOrCreate()
    val sc = spark.sparkContext

    val mccConf = new MultiClusterConf(args, sc)
    val hbaseContext = new HBaseContext(sc, mccConf.getConfiguration())
    val hdfsFs = sc.hadoopConfiguration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)
    val hdfsCheckpointDir = "%s%s".format( hdfsFs, sc.getConf.get("spark.kafkatohbase.checkpoint.dir") )

    import spark.implicits._

    val kafkaDF = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", sc.getConf.get("spark.kafkatohbase.kafka.bootstrap.servers"))
      .option("kafka.security.protocol", sc.getConf.get("spark.kafkatohbase.kafka.security.protocol"))
      .option("kafka.ssl.truststore.location", sc.getConf.get("spark.kafkatohbase.kafka.ssl.truststore.location"))
      .option("kafka.ssl.truststore.password", sc.getConf.get("spark.kafkatohbase.kafka.ssl.truststore.password"))
      .option("kafka.sasl.kerberos.service.name","kafka")
      .option("input.kafka.sasl.jaas.config",
        "com.sun.security.auth.module.Krb5LoginModule required doNotPrompt=true useKeyTab=true storeKey=true keyTab=\"%s\" principal=\"%s\"".format(
          sc.getConf.get("spark.kafkatohbase.keytab"),
          sc.getConf.get("spark.kafkatohbase.principal")
        ))
      .option("subscribe",sc.getConf.get("spark.kafkatohbase.source.kafka.topic"))
      .option("startingOffsets", sc.getConf.get("spark.kafkatohbase.startingOffsets"))
      .load()

    val rawDF = kafkaDF.selectExpr("CAST(value AS STRING)").as[String]

    val expandedDF = rawDF.map(row => row.split(","))
      .map(cols => HBaseData(
        cols(0),
        cols(1)
      ))

    val query = expandedDF
      .writeStream
      .option("checkpointLocation", hdfsCheckpointDir)
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .foreachBatch((batch_df: Dataset[HBaseData], batchId: Long) => {
        try {
          //Doing DF Write -----------------------------------------------------------------
          println("DF WRITE")
          println("Trying to write a DF to HBase")
          val hbaseTableSchema ="""rowKey STRING :key, rowValue STRING cf1:data"""
          val hbaseTableRowKeyList = List("rowKey")
          val targetTableColumns = List("rowKey","rowValue")
          val rowKeyCol: Column = hbaseTableRowKeyList.map(col).foldLeft(lit("")){ (acc, colVal) =>
            when(colVal.isNull,acc).otherwise(concat(acc,colVal))
          }

          batch_df.withColumn("rowKey", rowKeyCol).select(targetTableColumns.map(x => col(x.trim())):_*)
            .write.format("org.apache.hadoop.hbase.spark")
            .options(Map("hbase.columns.mapping" -> hbaseTableSchema, "hbase.table" -> sc.getConf.get("spark.kafkatohbase.target.hbase.table")))
            .save()
          println("DF WRITE FINISHED")
        }
        catch {
          case e: Exception => {
            logger.error("Exception writing to HBase! ", e)
          }
        }
      }).start().awaitTermination()
  }
}
