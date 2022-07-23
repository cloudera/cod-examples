package com.cloudera.hbase.mcc.scala.test

import com.cloudera.hbase.mcc.{MultiClusterConf, ConfigConst}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory
import com.cloudera.hbase.mcc.credentials.CredentialsManager
import org.apache.hadoop.fs.CommonConfigurationKeysPublic

object SparkToHbase {
  val logger = LoggerFactory.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Spark to HBase Dataframe API").getOrCreate()
    val sc = spark.sparkContext

    val HBASE_CLIENT_CONNECTION_IMPL = "hbase.client.connection.impl"
    val HBASE_CLIENT_USER_PROVIDER_CLASS = "hbase.client.userprovider.class"
    val CONNECTION_IMPL = "com.cloudera.hbase.mcc.ConnectionMultiCluster"
    val primaryHBaseSite = args(0)
    val primaryCoreSite = args(1)
    val failoverHBaseSite = args(2)
    val failoverCoreSite = args(3)

    val mccConf = new MultiClusterConf
    mccConf.set(HBASE_CLIENT_CONNECTION_IMPL, CONNECTION_IMPL)
    //Sets the default FS to that of the cluster submitting the spark job
    mccConf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, sc.hadoopConfiguration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY))
    mccConf.set(HBASE_CLIENT_USER_PROVIDER_CLASS, ConfigConst.HBASE_MCC_USER_PROVIDER)


    val primary = HBaseConfiguration.create()
    primary.addResource(new Path(primaryHBaseSite))
    primary.addResource(new Path(primaryCoreSite))
    primary.set("hbase.client.retries.number", "1"); //Override Default Parameters
    primary.set("hbase.client.pause", "1"); //Override Default Parameters
    primary.set("zookeeper.recovery.retry", "0"); //Override Default Parameters


    val failover = HBaseConfiguration.create()
    failover.addResource(new Path(failoverHBaseSite))
    failover.addResource(new Path(failoverCoreSite))
    failover.set("hbase.client.retries.number", "1"); //Override Default Parameters
    failover.set("hbase.client.pause", "1"); //Override Default Parameters

    val credentialsManager = CredentialsManager.getInstance
    primary.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(primaryHBaseSite, primaryCoreSite, sc))
    failover.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(failoverHBaseSite, failoverCoreSite, sc))

    mccConf.addClusterConfig(primary)
    mccConf.addClusterConfig(failover)

    import spark.implicits._

    new HBaseContext(sc, mccConf.getConfiguration())

    val rdd = sc.parallelize(Seq(("rowkey","SparkToHbase","0","","1")))
    val df_withcol = rdd.toDF("rowKey", "application", "batchId", "timeStamp", "loaded_events")
    logger.info("Data frame to Hbase : " + df_withcol.show())
    val hbaseTableName = "test_table"
    val hbaseTableSchema ="""rowKey STRING :key, application STRING cf1:APP, batchId STRING cf1:BID, timeStamp STRING cf1:TS, loaded_events STRING cf1:PR"""
    logger.info("Started writing to Hbase")
    df_withcol.write.format("org.apache.hadoop.hbase.spark")
      .options(Map("hbase.columns.mapping" -> hbaseTableSchema, "hbase.table" -> hbaseTableName))
      .save()
    logger.info("Completed writing to hbase")
    sc.stop()

  }
}
