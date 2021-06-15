# Stock ticker tracking

How to start the StockApp
---

First, make sure you have updated `pom.xml` with your COD database version information.

1. Run `mvn clean install` to build your application
1. `cp config-template.yml config.yml` and replace your connection information
1. Create the tables `DB_USERNAME='cdp_username' DB_PASSWORD='cdp_password' java -jar target/phoenix-stocks-0.1.0.jar create-tables config.yml`
1. Start application with `DB_USERNAME='cdp_username' DB_PASSWORD='cdp_password' java -jar target/phoenix-stocks-0.1.0.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`


Create some companies
---

```
$ curl -H 'Content-Type: application/json' --data '[{"tickerName":"CLDR", "companyName":"Cloudera"}, {"tickerName":"IBM", "companyName":"IBM"}]' http://localhost:8080/company/create
$ curl -H 'Content-Type: application/json' --data '[{"tickerName":"UBER", "companyName":"Uber"}, {"tickerName":"TSLA", "companyName":"Tesla, Inc"}, {"tickerName":"NVDA","companyName":"NVIDIA Corporation"}]' http://localhost:8080/company/create
$ curl -H 'Content-Type: application/json' --data '[{"tickerName":"AAPL", "companyName":"Apple"}, {"tickerName":"LYFT", "companyName":"Lyft"}, {"tickerName":"AMZN","companyName":"Amazon.com"}]' http://localhost:8080/company/create
```

Fetch some ticker values
---

`DB_USERNAME='csso_username' DB_PASSWORD='myspecialpassword' java -jar target/odx-example-1.0-SNAPSHOT.jar write-ticker-values  -a <alphafinance_api_key> config.yml`

See companies
---

`curl http://localhost:8080/`

or http://localhost:8080/

See the ticker for a company
---

`curl http://localhost:8080/value/0`

Clean up after
---

`DB_USERNAME='csso_username' DB_PASSWORD='myspecialpassword' java -jar target/odx-example-1.0-SNAPSHOT.jar delete-tables config.yml`
