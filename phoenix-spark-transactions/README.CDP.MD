# Prerequisites

1. Ensure Phoenix and Spark Installed In CDP-DC

2. Configure Phoenix Spark Connector using Cloudera Manager

https://docs.cloudera.com/runtime/7.2.12/phoenix-access-data/topics/phoenix-configure-spark-connector.html

# Set HBase and Phoenix verions in your Maven project

Login to gateway node and run following command to get the HBase version.

```
echo "HBase version"
hbase --version
```

Get Phoenix Spark version from the phoenix spark shaded jar path from the following location.
```
 /opt/cloudera/parcels/CDH/lib/phoenix_connectors/phoenix5-spark-shaded-[***VERSION***].jar
```

```
HBase Version
2.4.6.7.2.14.0-133
Phoenix Spark Version
"6.0.0.7.2.14.0-133"
```

Finally, update the HBase and Phoenix Connector versions in our Maven project/configuration.
```
<properties>
...
    <phoenix.connector.version>6.0.0.7.2.14.0-133</phoenix.connector.version>
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
spark-submit --master local[2] --jars /opt/cloudera/parcels/CDH/lib/phoenix_connectors/phoenix5-spark-shaded-6.0.0.7.2.14.0-117.jar --class com.cloudera.cod.examples.spark.SparkApp ./target/phoenix-spark-transactions-0.1.0.jar jdbc:phoenix:[***zookeeper_quorum***]:[***zookeeper_port***]:[***zookeeper_hbase_path***]

```

