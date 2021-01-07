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
using System;
using System.Data.Odbc;

namespace PhoenixClient
{
    class Program
    {
        private static string hostname = "<hostname>";
        private static string httppath = "<httppath>";
        private static string username = "<username>";
        private static string encryptedPwd = "<encrypted_password>";
        private static string tableName = "BUBU";

        static void Main(string[] args)
        {
            Console.WriteLine("Hello ODBC!");
            
            string connString =
                @"Driver={Hortonworks Phoenix ODBC Driver};ssl=1;allowhostnamecnmismatch=1;allowselfsignedservercert=1;authmech=1;port=443;usesystemtruststore=1;"
                + "httppath=" + httppath + ";"
                + "host=" + hostname + ";"
                + "uid=" + username + ";"
                + "encryptedpwd=" + encryptedPwd + ";";

            OdbcConnection cnn = new OdbcConnection(connString);
            cnn.Open();

            Console.WriteLine("Connected to remote ODBC server");

            OdbcCommand cmd = cnn.CreateCommand();
            cmd.CommandText = "INSERT INTO \"PHOENIX#\"." + tableName + " (ID) VALUES (20)";
            cmd.ExecuteNonQuery();
            cmd.CommandText = "INSERT INTO \"PHOENIX#\"." + tableName + " (ID) VALUES (30)";
            cmd.ExecuteNonQuery();
            cmd.CommandText = "INSERT INTO \"PHOENIX#\"." + tableName + " (ID) VALUES (50)";
            cmd.ExecuteNonQuery();

            Console.WriteLine("Records written successfully");

            cmd.CommandText = "SELECT * FROM \"PHOENIX#\"." + tableName;
            OdbcDataReader reader = cmd.ExecuteReader();

            Console.WriteLine("Table fields:");

            int fCount = reader.FieldCount;
            Console.Write(":");
            for (int i = 0; i < fCount; i++)
            {
                string fName = reader.GetName(i);
                Console.Write(fName + ":");
            }
            Console.WriteLine();

            Console.WriteLine("Table rows:");

            while (reader.Read())
            {
                Console.Write(":");
                for (int i = 0; i < fCount; i++)
                {
                    string col = reader.GetString(i);
                    Console.Write(col + ":");
                }
                Console.WriteLine();
            }

            reader.Close();
            cmd.Dispose();
            cnn.Close();

            Console.WriteLine("Done.");
            Console.ReadKey();
        }
    }
}
