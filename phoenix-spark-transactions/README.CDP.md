# Prerequisites

1. Ensure Phoenix and Spark Installed In CDP Private Cloud Base

2. Configure Phoenix Spark Connector using Cloudera Manager

https://docs.cloudera.com/runtime/7.2.12/phoenix-access-data/topics/phoenix-configure-spark-connector.html

# Set HBase and Phoenix verions in your Maven project

Login to gateway node and run following command to get the HBase version.

```
echo "HBase version"
hbase --version
```

```
HBase Version
2.4.6.7.2.14.0-133
```

Finally, update the HBase and Phoenix Connector versions in our Maven project/configuration.
```
<properties>
...
    <hbase.version>2.4.6.7.2.14.0-133</hbase.version>
...
</properties>
```

# Build the project

```
$ mvn package
```

# Run the project
Run the Spark job example:


```
spark-submit --master local[2] --class com.cloudera.cod.examples.spark.SparkApp ./target/phoenix-spark-transactions-0.1.0.jar jdbc:phoenix:[***zookeeper_quorum***]:[***zookeeper_port***]:[***zookeeper_hbase_path***]

```

