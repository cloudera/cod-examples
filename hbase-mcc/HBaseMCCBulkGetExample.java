package com.cloudeara.hbase.mcc.java;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import com.cloudera.hbase.mcc.MultiClusterConf;
import com.cloudera.hbase.mcc.ConfigConst;
import com.cloudera.hbase.mcc.credentials.CredentialsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class HBaseMCCBulkGetExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        SparkConf conf = new SparkConf().setAppName("JavaHBaseMCCBulkGetExample");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        String tableName = "test_table";
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
            List<byte[]> list = new ArrayList<byte[]>(7);
            list.add(Bytes.toBytes("1"));
            list.add(Bytes.toBytes("2"));
            list.add(Bytes.toBytes("3"));
            list.add(Bytes.toBytes("4"));
            list.add(Bytes.toBytes("5"));
            list.add(Bytes.toBytes("6"));
            list.add(Bytes.toBytes("7"));

            JavaRDD<byte[]> rdd = jsc.parallelize(list);

            JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, mccConf.getConfiguration());

            JavaRDD<String> resultRDD = hbaseContext.bulkGet(TableName.valueOf(tableName), 2, rdd, new GetFunction(),
                    new ResultFunction());
            // Print the results
            for (String resultStr : resultRDD.collect()) {
                System.out.println(resultStr);
            }

        } finally {
            jsc.stop();
        }
    }

    public static class GetFunction implements Function<byte[], Get> {

        private static final long serialVersionUID = 1L;

        public Get call(byte[] v) throws Exception {
            return new Get(v);
        }
    }

    public static class ResultFunction implements Function<Result, String> {
        private static final long serialVersionUID = 1L;

        public String call(Result result) throws Exception {
            Iterator<Cell> it = result.listCells().iterator();
            StringBuilder b = new StringBuilder();
            b.append(Bytes.toString(result.getRow())).append(":");
            while (it.hasNext()) {
                Cell cell = it.next();
                String q = Bytes.toString(cell.getQualifierArray());
                if (q.equals("counter")) {
                    b.append("(").append(Bytes.toString(cell.getQualifierArray())).append(",")
                            .append(Bytes.toLong(cell.getValueArray())).append(")");
                } else {
                    b.append("(").append(Bytes.toString(cell.getQualifierArray())).append(",")
                            .append(Bytes.toString(cell.getValueArray())).append(")");
                }
            }
            return b.toString();
        }
    }
}
