<#-- @ftlvariable name="" type="com.github.joshelser.views.CompanyView" -->
<html>
    <body>
        <h1>Cloudera Operational Database</h1>
        <p>Name: ${company.companyName}, Symbol: ${company.tickerName}</p>
        <p><a href="/value/${company.id}">Stock Values</a></p>
        <p><a href="/">All Companies</a></p>
    </body>
</html>