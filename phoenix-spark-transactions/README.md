# Prerequisites

1. You have set your CDP workload password:

https://docs.cloudera.com/management-console/cloud/user-management/topics/mc-setting-the-ipa-password.html

2. You have synchronized users from User Management Service in the CDP Control Plane into the environment
in which your COD database is running.

3. Ensure CDE service enabled and virtual cluster created in Data Engineering Experiance

https://docs.cloudera.com/data-engineering/cloud/enable-data-engineering/topics/cde-enable-data-engineering.html
https://docs.cloudera.com/data-engineering/cloud/manage-clusters/topics/cde-create-cluster.html

# Set HBase and Phoenix verions in your Maven project

From the `describe-client-connectivity` call, we can get the HBase and Phoenix version information. This code snippet
shows fetching the database connectivity information and parsing the required HBase and Phoenix information to build your
application.
```
echo "HBase version"
cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq ".connectors[] | select(.name == \"hbase\") | .version"
echo "Phoenix Connector Version"
cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq ".connectors[] | select(.name == \"phoenix-thin-jdbc\") | .version"
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

# Download hbase-site.xml and hbase-omid-client-config.yml

Use the `describe-client-connectivity` endpoint to determine the client config url.

```
cdp opdb describe-client-connectivity --database-name spark-connector --environment-name cod-7213 | jq ".connectors[] | select(.name == \"hbase\") | .configuration.clientConfigurationDetails[] | select(.name == \"HBASE\") | .url "
```

output:
```
"https://cod--XXXXXX-gateway0..xcu2-8y8x.dev.cldr.work/clouderamanager/api/v41/clusters/XXXXX/services/hbase/clientConfig"
```

Run curl command to download the hbase configurations by using url collected from the above command.

```
curl -f -o "hbase-config.zip" -u "<csso_user>" "https://cod--XXXXXX-gateway0.cod-7213....xcu2-8y8x.dev.cldr.work/clouderamanager/api/v41/clusters/cod--XXXX/services/hbase/clientConfig"
```
Unzip hbase-config.zip and copy the hbase-site.xml and hbase-omid-client-config.yml to src/main/resources path in Maven project.

```
unzip hbase-conf.zip
cp hbase-conf/hbase-site.xml <path to src/main/resources>
cp hbase-conf/hbase-omid-client-config.yml <path to src/main/resources>

```

# Build the project

```
$ mvn package
```

# Create CDE job

Configure CDE CLI to point to the virtual cluster. Refer below link.
https://docs.cloudera.com/data-engineering/cloud/cli-access/topics/cde-download-cli.html

Create resource using the following command.
```
cde resource create --name phoenix-spark-app-resource
```

Upload required jars that were downloaded during build.
```
cde resource upload --name spark-app-resource --local-path ./target/connector-libs/hbase-shaded-mapreduce-2.4.6.7.2.14.0-133.jar --resource-path hbase-shaded-mapreduce-2.4.6.7.2.14.0-133.jar
cde resource upload --name spark-app-resource --local-path ./target/connector-libs/opentelemetry-api-0.12.0.jar --resource-path opentelemetry-api-0.12.0.jar
cde resource upload --name spark-app-resource --local-path ./target/connector-libs/opentelemetry-context-0.12.0.jar --resource-path opentelemetry-context-0.12.0.jar
cde resource upload --name spark-app-resource --local-path ./target/connector-libs/phoenix5-spark-shaded-6.0.0.7.2.14.0-133.jar --resource-path phoenix5-spark-shaded-6.0.0.7.2.14.0-133.jar
```
Upload the spark application app jar that was built earlier.

```
cde resource upload --name spark-app-resource --local-path ./target/phoenix-spark-transactions-0.1.0.jar --resource-path phoenix-spark-transactions-0.1.0.jar
```

Replace HBase, Phoenix, Phoenix spark connector versions in the spark-job.json as below and create CDE job using the following json and import command.
```
{
   "mounts":[
      {
         "resourceName":"phoenix-spark-app-resource"
      }
   ],
   "name":"phoenix-spark-app",
   "spark":{
      "className":"com.cloudera.cod.examples.spark.SparkApp",
      "args":[
         "{{ phoenix_jdbc_url }}"
      ],
      "driverCores":1,
      "driverMemory":"1g",
      "executorCores":1,
      "executorMemory":"1g",
      "file":"phoenix-spark-transactions-0.1.0.jar",
      "pyFiles":[
         
      ],
      "files":[
        "hbase-shaded-mapreduce-2.4.6.7.2.14.0-133.jar",
        "opentelemetry-api-0.12.0.jar",
        "opentelemetry-context-0.12.0.jar"
        "phoenix5-spark-shaded-6.0.0.7.2.14.0-133.jar",
      ],
      "numExecutors":4
   }
}
```

```
cde job import --file spark-job.json
```


# Run the project
Run the Spark job example:

Again, use the `describe-client-connectivity` endpoint to determine the base JDBC url to provide.
```
cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq ".connectors[] | select(.name == \"phoenix-thick-jdbc\") | .configuration.jdbcUrl"
```

Run the job by passing the above jdbc url as argument to the job.

```
cde job run --name phoenix-spark-app --variable phoenix_jdbc_url=<phoenix_jdbc_url>​
```

