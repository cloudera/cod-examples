object StructuredStreamingHDFSToHBase {

  def main(args: Array[String]): Unit = {
    //Creating an array of configurations for MCC, must be evenly paired
    val config_args = new Array[String](4)
    config_args(0) = args(0)
    config_args(1) = args(1)
    config_args(2) = args(2)
    config_args(3) = args(3)

    val spark = SparkSession
      .builder()
      .config("spark.eventLog.enabled", "false")
      .config("spark.driver.memory", "2g")
      .config("spark.executor.memory", "2g")
      .appName(Structured Streaming HDFS to HBase)
      .getOrCreate()
    val sc = spark.sparkContext
    import spark.implicits._
    val mccConf = new MultiClusterConf(config_args, sc)
    val hdfsFs = sc.hadoopConfiguration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)

    //Variables set to be used with HDFS
    val hdfsSourceDir = "%s%s".format(hdfsFs, args(4))
    val hdfsCheckpointDir = "%s%s".format(hdfsFs, args(5))

    val dataSchema = new StructType()
      .add("key", "string")
      .add("value", "string");

    val hbaseContext = new HBaseContext(sc, mccConf.getConfiguration())

    //Read input
    val dataStream = spark.readStream.schema(dataSchema).csv(hdfsSourceDir)
    dataStream.createOrReplaceTempView("data_stream")
    val sql = "SELECT * FROM data_stream"
    val allData = spark.sql(sql)

    val query = allData
      .writeStream
      .option("checkpointLocation", hdfsCheckpointDir)
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .foreachBatch((batch_df: Dataset[Row], batchId: Long) => {
        try {
          // Doing DF Write -----------------------------------------------------------------
          val hbaseTableSchema ="""key STRING :key, value STRING cf1:data"""
          val hbaseTableRowKeyList = List("key")
          val targetTableColumns = List("key","value")
          val rowKeyCol: Column = hbaseTableRowKeyList.map(col).foldLeft(lit("")){ (acc, colVal) =>
            when(colVal.isNull,acc).otherwise(concat(acc,colVal))
          }
          batch_df.withColumn("key", rowKeyCol).select(targetTableColumns.map(x => col(x.trim())):_*)
            .write.format("org.apache.hadoop.hbase.spark")
            .options(Map("hbase.columns.mapping" -> hbaseTableSchema, "hbase.table" -> args(6)))
            .save()
        }
        catch {
          case e: Exception => {
            print(e)
          }
        }
      }).start()
      .awaitTermination()

  }
}
