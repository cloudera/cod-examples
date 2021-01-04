# Phoenix ODBC .NET Client

This example demonstrates how to create a Phoenix ODBC client with ADO.NET extensions in .NET
Framework 4.5.2 and above.

It uses the standard DSN-less connection string approach to connect to PhoenixDB and to get it 
working you need to set the following parameters in the connection string based on your 
environment's connectivity info:

- `hostname` : The Phoenix ODBC endpoint hostname without any prefix (http://) or port number,
- `http_path` : The HTTP path after the hostname,
- `username` : Workload username to login,
- `encryptedPwd` : The encrypted password,
- `tableName` : Name of the PhoenixDB table (BUBU in our example).

The program will add some records to the table at the beginning and retrieves them after that.
Schema of the table is very simple, consists of a single int-type private key.

The easiest way to populate the above parameters or the entire connection is to use Visual Studio
Wizard to create new Database Connection. The properties of the new connection will show the connection
string which could be copied into your code.

## Building the application

After configuring the above parameters (modifing the source files), this example can be either 
loaded and built in latest version of Visual Studio (community version is fine) or in command 
line via the .Net SDK.

## Running the application

Run in Visual Studio (F5) or run the compiled executable.
