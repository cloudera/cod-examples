package com.cloudera.hbase.mcc.scala.test

import org.apache.hadoop.fs.Path
import com.cloudera.hbase.mcc.MultiClusterConf
import org.apache.hadoop.hbase.spark.HBaseContext
import com.cloudera.hbase.mcc.credentials.CredentialsManager
import org.apache.spark.SparkContext
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Delete
import org.apache.spark.SparkConf

object HBaseMCCBulkDeleteExample {
  def main(args: Array[String]) {

    val tableName = "test_table"
    val columnFamily = "cf1"

    val sparkConf = new SparkConf().setAppName("HBaseBulkDeleteExample " + tableName)
    val sc = new SparkContext(sparkConf)

    val HBASE_CLIENT_CONNECTION_IMPL = "hbase.client.connection.impl"
    val HBASE_CLIENT_USER_PROVIDER_CLASS = "hbase.client.userprovider.class"
    val primaryHBaseSite = args(0)
    val primaryCoreSite = args(1)
    val failoverHBaseSite = args(2)
    val failoverCoreSite = args(3)

    val mccConf = new MultiClusterConf
    mccConf.set(HBASE_CLIENT_CONNECTION_IMPL, ConfigConst.HBASE_MCC_CONNECTION_IMPL)
    //Sets the default FS to that of the cluster submitting the spark job
    mccConf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, sc.hadoopConfiguration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY))
    mccConf.set(HBASE_CLIENT_USER_PROVIDER_CLASS, ConfigConst.HBASE_MCC_USER_PROVIDER)

    val primary = HBaseConfiguration.create()
    primary.addResource(new Path(primaryHBaseSite))
    primary.addResource(new Path(primaryCoreSite))

    val failover = HBaseConfiguration.create()
    failover.addResource(new Path(failoverHBaseSite))
    failover.addResource(new Path(failoverCoreSite))

    val credentialsManager = CredentialsManager.getInstance
    primary.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(primaryHBaseSite, primaryCoreSite, sc))
    failover.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(failoverHBaseSite, failoverCoreSite, sc))

    mccConf.addClusterConfig(primary)
    mccConf.addClusterConfig(failover)
    try {
      //[Array[Byte]]
      val rdd = sc.parallelize(Array(
        Bytes.toBytes("1"),
        Bytes.toBytes("2"),
        Bytes.toBytes("3"),
        Bytes.toBytes("4"),
        Bytes.toBytes("5"),
        Bytes.toBytes("6"),
        Bytes.toBytes("7")
      ))

      val hbaseContext = new HBaseContext(sc, mccConf.getConfiguration())
      hbaseContext.bulkDelete[Array[Byte]](rdd,
        TableName.valueOf(tableName),
        putRecord => new Delete(putRecord),
        4)
    } finally {
      sc.stop()
    }
  }
}
