# Prerequisites

1. You have set your CDP workload password:

https://docs.cloudera.com/management-console/cloud/user-management/topics/mc-setting-the-ipa-password.html

2. You have synchronized users from User Management Service in the CDP Control Plane into the environment
in which your COD database is running.
   
# Configure environment

1. Change baseurl to your own rest server url in common.py

2. Please set environment variables in your dev environment.

    1. Open .bash_profile by using:
        ```
        vim ~/.bash-profile
        ```
    2. Use the export command to add new environment variables:
        ```
        export DB_USER=[your_workload_username]
        export DB_PASS=[your_workload_password]
        ```
    3. Save any changes you made to the .bash_profile file.
    4. Execute the new .bash_profile by either restarting the terminal window or using:
        ```
        source ~/.bash-profile
        ```
    5. If you are using zsh, please update .zshrc file

# Run example
1. Run write_example.py
```
~/YourWorkSpace/hbase-rest-read-write/cloudera/cod/examples/wrie_example.py
```
2. Run read_example.py
```
~/YourWorkSpace/hbase-rest-read-write/cloudera/cod/examples/read_example.py
```