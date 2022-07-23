package com.cloudera.hbase.mcc.scala.test

import com.cloudera.hbase.mcc.{ConfigConst, MultiClusterConf}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.HBaseConfiguration
import com.cloudera.hbase.mcc.credentials.CredentialsManager
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.cloudera.hbase.mcc.ConnectionMultiCluster
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.fs.CommonConfigurationKeysPublic

object SparkStreamingExample {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkStreamingExample")
    lazy val ssc = new StreamingContext(sparkConf, Seconds(20))

    //Variables that must be passed into the tasks, scc is not serializable
    val hdfsFs = ssc.sparkContext.hadoopConfiguration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)

    //Variables set to be used with HDFS
    val hdfsSourceDir = "%s/user/exampleuser/SparkStreamingExample/".format(hdfsFs)
    val hdfsCheckpointDir = "%s/user/exampleuser/SparkStreamingExampleCheckpoint/".format(hdfsFs)

    //HBase and Core site pulled from --files
    val primaryHBaseSite = args(0)
    val primaryCoreSite = args(1)
    val failoverHBaseSite = args(2)
    val failoverCoreSite = args(3)

    //Token file names created by driver are required to be known in the executor and must be passed with this param
    //for each of the respective configurations
    val credentialsManager = CredentialsManager.getInstance
    val cluster1TokenFile = credentialsManager.confTokenForCluster(primaryHBaseSite, primaryCoreSite, ssc)
    val cluster2TokenFile = credentialsManager.confTokenForCluster(failoverHBaseSite, failoverCoreSite, ssc)

    //Create a function that stores state of the previous word count ---------------------------------------------------
    val updateData = (values: Seq[Int], state: Option[Int]) =>{
      val currentCount = values.foldLeft(0)(_ + _)
      val previousCount = state.getOrElse(0)
      val updatedSum = currentCount + previousCount
      Some(updatedSum)
    }

    //HDFS -------------------------------------------------------------------------------------------------------------
    //Read from the HDFS directory, will pull in new files as they land
    val lines = ssc.textFileStream(hdfsSourceDir)
    //get the words fr om each line by looking for spaces
    val words = lines.flatMap(_.split(" "))
    //get the count for each word
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _).updateStateByKey(updateData)

    //HBase ------------------------------------------------------------------------------------------------------------
    //Set a checkpoint when using spark streaming with hbase
    ssc.checkpoint(hdfsCheckpointDir)

    //Set spark table variables
    val tableName = "spark_streaming_wc"
    val familyName = "word_count"
    val colName = "count"

    //Create a function to load the data to HBase
    def putHBase(row: (_,_)): Unit = {
      //Set up the multi-cluster configuration for HBase
      val mccConf = new MultiClusterConf
      //Sets the default FS to that of the cluster submitting the spark job
      mccConf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, hdfsFs)

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

      primary.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, cluster1TokenFile)
      failover.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, cluster2TokenFile)

      //Add the final hbase configurations to MCC
      mccConf.addClusterConfig(primary)
      mccConf.addClusterConfig(failover)

      //Get the connection from Multi-cluster
      val connection = new ConnectionMultiCluster(mccConf.getConfiguration)
      //Get an instance of table
      val table = connection.getTable(TableName.valueOf(tableName))
      val put = new Put( Bytes.toBytes( row._1.toString ) )
      put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(colName), Bytes.toBytes(row._2.toString))
      table.put(put)
    }

    //Call the function for each one of the word counts in the array
    wordCounts.foreachRDD(rdd => if (!rdd.isEmpty()) rdd.foreach(putHBase(_)))

    ssc.start()
    ssc.awaitTermination()
  }
}
