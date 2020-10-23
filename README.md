# Cloudera Operational Database Examples

This repository is a collection of examples which demonstrate self-contained applications
running against the Cloudera Operational Database (COD) Experience.

These are "getting started" examples which are designed to be standalone and demonstrate
the developer's path. It is expected that, for each example, you will modify some part
of it to connect the application to your COD database.

For examples which use Apache Maven, you will need to place the proper Maven repository
URL from your COD Database client connectivity information into the pom.xml file.

## Included examples

* [Apache HBase (NoSQL), Java, Maven: Read-Write](hbase-read-write/README.md)

A Java application which creates an HBase table, writes some records, and validates that
it can read those records from the table via the HBase Java API.

* [Apache Phoenix (SQL), Java, Maven: Read-Write](phoenix-read-write/README.md)

A Java application which creates a Phoenix table, writes some rows, and validates that
it can read those rows back from the table via Phoenix JDBC API. Variants exist for the
Phoenix thick JDBC driver and Phoenix thin JDBC driver.

* [Apache Phoenix (SQL), Python: Read-Write](phoenixdb-read-write/README.md)

A Python application built on Flask which creates a simple blog using the Phoenix
Python adapter.

* [Apache Phoenix (SQL), Java, Dropwizard: Stock ticker](phoenix-stock-ticker/README.md)

A Java application built on Dropwizard which is a simple tracker for the price of
various company's stock prices.
