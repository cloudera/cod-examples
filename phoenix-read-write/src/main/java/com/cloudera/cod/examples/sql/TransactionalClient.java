/*
 * Copyright 2021 Cloudera, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.cod.examples.sql;

import java.sql.SQLException;

public class TransactionalClient extends AbstractSqlClient {

  public static void main(String[] args) throws SQLException {
    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: com.cloudera.cod.examples.sql.TransactionalClient \"jdbc:phoenix:host:2181:/hbase\"");
    }
    TransactionalClient c = new TransactionalClient();
    final String clusterKey = args[0].trim();
    c.runTransactions(clusterKey);
  }
}
