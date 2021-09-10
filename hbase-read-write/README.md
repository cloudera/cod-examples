# Prerequisites

1. You have set your CDP workload password:

https://docs.cloudera.com/management-console/cloud/user-management/topics/mc-setting-the-ipa-password.html

2. You have synchronized users from User Management Service in the CDP Control Plane into the environment
in which your COD database is running.

# Set HBase version in your Maven project

From the `describe-client-connectivity` call, we can get the HBase version information. This code snippet
shows fetching the database connectivity information and parsing the required HBase information to build your
application.
```
$ cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env  | jq '.connectors[] | select(.name == "hbase")'
{
  "name": "hbase",
  "version": "2.2.6.7.2.9.0-203",
  "kind": "LIBRARY",
  "dependencies": {
    "mavenUrl": "https://host.cloudera.site/.../cdp-proxy-api/hbase/jars"
  },
  "configuration": {
    "clientConfigurationUrl": "https://host.cloudera.site/clouderamanager/api/v41/clusters/.../services/hbase/clientConfig"
  },
  "requiresKerberos": true
}
```

In the above JSON, we need to use the `version` attribute in our Maven project/configuration.

```
<project>
  <dependencies>
    <!-- NoSQL client for COD -->
    <dependency>
      <groupId>org.apache.hbase</groupId>
      <artifactId>hbase-shaded-client</artifactId>
      <version>2.2.6.7.2.9.0-203</version>
    </dependency>
  </dependencies>
  ...
</project>
```

# Build the project

```
$ mvn package
```

# Run the project

Before we can run an example for the HBase client, we must observe that the `requiredKerberos` option is set to `true`
for this connector. This means that we *must* run this from a computer which:

* Has internal network access to the VPC the database is deployed in
* Can resolve the internal hostnames of the database
* Can obtain a Kerberos ticket from the database's KDC

Very likely, an end-user's computer is _not_ able to execute these commands. The easiest method to run traditional HBase
client applications is to launch a "edge node" in your cloud provider which meets the above requirements. See the COD
documentation for more information.

Finally, ensure that you download the directory containing HBase client configuration files from the
`clientConfigurationUrl` provided in `describe-client-connectivity` response. This is a protected endpoint -- again, you
should use your CDP workload credentials.

After the pre-requisites are met, we can use the `clientConfigurationUrl` from `describe-client-connectivity` to obtain
the necessary configuration clients to talk to HBase.

```
$ cdp opdb describe-client-connectivity --database-name my-database --environment-name my-env | jq '.connectors[] | select(.name == "hbase") | .configuration.clientConfigurationDetails[0].url'
"https://host.cloudera.site/clouderamanager/api/v41/clusters/.../services/hbase/clientConfig"
$ curl -o clientConfig.zip -u 'cdp_username' 'https://host.cloudera.site/clouderamanager/api/v41/clusters/.../services/hbase/clientConfig'
```

Aside, you may find it easier to build on your local machine and simply copy the JAR files to the remote node, rather
than set install a JDK and Apache Maven on your edge node.

```
$ scp -r target ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com:
$ scp clientConfig.zip ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com:
$ ssh ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com "sudo yum install -y java-1.8.0-openjdk"
$ ssh ec2-user@my-ec2-edge-node.us-west-2.compute.amazonaws.com "unzip clientConfig.zip"
```

Finally, ensure that you have a Kerberos ticket, and then run the example.
```
$ kinit cdp_username
Password: ***********
$ export JAVA_HOME="/usr/lib/jvm/java-1.8.0"
$ java -cp target/nosql-libs/*:target/hbase-read-write-0.1.0.jar:hbase-conf com.cloudera.cod.examples.nosql.ReadWriteExample
```
