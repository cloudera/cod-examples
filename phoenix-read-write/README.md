# Prerequisites

1. You have set your CDP workload password:

https://docs.cloudera.com/management-console/cloud/user-management/topics/mc-setting-the-ipa-password.html

2. You have synchronized users from User Management Service in the CDP Control Plane into the environment
in which your COD database is running.

# Set Phoenix verions in your Maven project

From the `describe-client-connectivity` call, we can get the Phoenix version information. This code snippet
shows fetching the database connectivity information and parsing the required Phoenix information to build your
application.
```
$ for flavor in thick thin; do
  echo "Phoenix-$flavor"
  cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq ".connectors[] | select(.name == \"phoenix-$flavor-jdbc\") | .version"
done
```

```
Phoenix-thick
"https://gateway.cloudera.site/.../cdp-proxy-api/avatica/maven"
"5.1.1.7.2.9.0-203"
Phoenix-thin
"https://gateway.cloudera.site/.../cdp-proxy-api/avatica/maven"
"6.0.0.7.2.9.0-203"
```

Finally, update the Phoenix versions in our Maven project/configuration.
For Cloudera Runtime 7.2.8 and before use `phoenix-client` artifactId instead of `phoenix-client-hbase-2.2` for dependency and also update the `includeArtifactIds` with `phoenix-client,phoenix-queryserver-client`
```
<project>
  <dependencies>
    <dependency>
      <groupId>org.apache.phoenix</groupId>
      <artifactId>phoenix-client-hbase-2.2</artifactId>
      <!-- Phoenix thick client version given by COD -->
      <version>5.1.1.7.2.9.0-203</version>
    </dependency>
    <dependency>
      <groupId>org.apache.phoenix</groupId>
      <artifactId>phoenix-queryserver-client</artifactId>
      <!-- Phoenix thin client version given by COD -->
      <version>6.0.0.7.2.9.0-203</version>
    </dependency>
  </dependencies>
    <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.2</version>
        <executions>
          <execution>
            <id>copy-sql-dependencies</id>
            <phase>package</phase>
            <goals>
              <goal>copy-dependencies</goal>
            </goals>
            <configuration>
              <!-- Use phoenix-client artifactId for Cloudera Runtime 7.2.8 and before -->
              <!-- <includeArtifactIds>phoenix-client,phoenix-queryserver-client</includeArtifactIds> -->
              <includeArtifactIds>phoenix-client-hbase-2.2,phoenix-queryserver-client</includeArtifactIds>
              <outputDirectory>${project.build.directory}/sql-libs/</outputDirectory>
              <overWriteReleases>false</overWriteReleases>
              <overWriteSnapshots>false</overWriteSnapshots>
              <overWriteIfNewer>true</overWriteIfNewer>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

# Build the project

```
$ mvn package
```

# Run the project
Run the SQL example:

Again, use the `describe-client-connectivity` endpoint to determine the base JDBC url to provide.
```
$ for flavor in thick thin; do
  echo "Phoenix-$flavor"
  cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq ".connectors[] | select(.name == \"phoenix-$flavor-jdbc\") | .configuration.jdbcUrl"
done
```

```
Phoenix-thick
"jdbc:phoenix:host1.cloudera.site,host2.cloudera.site,host3.cloudera.site:2181:/hbase"
Phoenix-thin
"jdbc:phoenix:thin:url=https://gateway.cloudera.site/.../cdp-proxy-api/avatica/;serialization=PROTOBUF;authentication=BASIC"
```

## Running the thick client example

Before we can run an example for the Phoenix Thick client, we must observe that the `requiredKerberos` option is set to `true`
for the Thick client. This means that we *must* run this from a computer which:

* Has internal network access to the VPC the database is deployed in
* Can resolve the internal hostnames of the database
* Can obtain a Kerberos ticket from the database's KDC

Very likely, an end-user's computer is _not_ able to execute these commands. The easiest method to run traditional Phoenix Thick
applications is to launch a "edge node" in your cloud provider which meets the above requirements. See the edge node documentation.

After the pre-requisites are met, we can use the JDBC url from `describe-client-connectivity` to run the example. You may find it
easier to build on your local machine and simply copy the JAR files to the remote node.

```
$ scp -r target ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com:
$ scp clientConfig.zip ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com:
$ ssh ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com "sudo yum install -y java-1.8.0-openjdk"
```

Finally, ensure that you have a Kerberos ticket, and then run the example.
```
$ kinit <cdp_username>
$ java -cp target/sql-libs/*:target/phoenix-read-write-0.1.0.jar:hbase-conf com.cloudera.cod.examples.sql.Client "jdbc:phoenix:node1.cloudera.site,node2.cloudera.site,node3.cloudera.site:2181:/hbase"
```

## Running the thin client example

First, we can observe that because this connector (phoenix-thin-driver) has `requiredKerberos` set to `false`, that means
we can use it from virtually any node. This example, we'll run a client from our local machine.

For the thin client, the `describe-client-connectivity` call returns a base JDBC url to use. You must append the following attributes to the URL which are specific to your identity.

* `avatica_user`: your CDP username _(required)_
* `avatica_password`: your CDP workload password _(required)_
* `truststore`: A truststore for your CDP Knox gateway _(optional)_
* `truststore_password`: The password for the truststore file _(optional)_

We can use Maven to ease launching this application, but a standalone Java program is similarly launched.

```
$ mvn exec:exec -Dexec.executable=java -Dexec.args='-cp target/sql-libs/*:target/phoenix-read-write-0.1.0.jar com.cloudera.cod.examples.sql.ThinClient "jdbc:phoenix:thin:url=https://host.cloudera.site/.../cdp-proxy-api/avatica/;serialization=PROTOBUF;authentication=BASIC;avatica_user=workloadUsername;avatica_password=workloadPassword"'
```

Or, you can launch it without the help of Maven:

```
$ java -cp target/sql-libs/*:target/phoenix-read-write-0.1.0.jar com.cloudera.cod.examples.sql.ThinClient "jdbc:phoenix:thin:url=https://gateway.cloudera.site/.../cdp-proxy-api/avatica/;serialization=PROTOBUF;authentication=BASIC;avatica_user=workloadUsername;avatica_password=workloadPassword"
```
