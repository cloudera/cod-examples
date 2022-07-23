package com.cloudeara.hbase.mcc.java;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import com.cloudera.hbase.mcc.*;
import com.cloudera.hbase.mcc.credentials.CredentialsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseMCCBulkPutExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        SparkConf conf = new SparkConf().setAppName("JavaHBaseMCCBulkPutExample");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        String tableName = "test_table";
        String columnFamily = "cf1";
        String HBASE_CLIENT_CONNECTION_IMPL = "hbase.client.connection.impl";
        String HBASE_CLIENT_USER_PROVIDER_CLASS = "hbase.client.userprovider.class";

        String primaryHBaseSite = args[0];
        String primaryCoreSite = args[1];
        String failoverHBaseSite = args[2];
        String failoverCoreSite = args[3];

        MultiClusterConf mccConf = new MultiClusterConf();
        mccConf.set(HBASE_CLIENT_CONNECTION_IMPL, ConfigConst.HBASE_MCC_CONNECTION_IMPL);
        mccConf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY,
                jsc.sc().hadoopConfiguration().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY));
        mccConf.set(HBASE_CLIENT_USER_PROVIDER_CLASS, ConfigConst.HBASE_MCC_USER_PROVIDER);

        Configuration primary = HBaseConfiguration.create();
        primary.addResource(new Path(primaryHBaseSite));
        primary.addResource(new Path(primaryCoreSite));

        Configuration failover = HBaseConfiguration.create();
        failover.addResource(new Path(failoverHBaseSite));
        failover.addResource(new Path(failoverCoreSite));

        CredentialsManager credentialsManager = CredentialsManager.getInstance();
        primary.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(primaryHBaseSite, primaryCoreSite, jsc));
        failover.set(ConfigConst.HBASE_MCC_TOKEN_FILE_NAME, credentialsManager.confTokenForCluster(failoverHBaseSite, failoverCoreSite, jsc));

        mccConf.addClusterConfig(primary);
        mccConf.addClusterConfig(failover);

        try {
            List<String> list = new ArrayList<String>(7);
            list.add("1," + columnFamily + ",1,1");
            list.add("2," + columnFamily + ",1,2");
            list.add("3," + columnFamily + ",1,3");
            list.add("4," + columnFamily + ",1,4");
            list.add("5," + columnFamily + ",1,5");
            list.add("6," + columnFamily + ",1,6");
            list.add("7," + columnFamily + ",1,7");

            JavaRDD<String> rdd = jsc.parallelize(list);

            JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, mccConf.getConfiguration());

            hbaseContext.bulkPut(rdd, TableName.valueOf(tableName), new PutFunction());
        } finally {
            jsc.stop();
        }
    }

    public static class PutFunction implements Function<String, Put> {
        private static final long serialVersionUID = 1L;

        public Put call(String v) throws Exception {
            String[] cells = v.split(",");
            Put put = new Put(Bytes.toBytes(cells[0]));
            put.addColumn(Bytes.toBytes(cells[1]), Bytes.toBytes(cells[2]), Bytes.toBytes(cells[3]));
            return put;
        }
    }
}
