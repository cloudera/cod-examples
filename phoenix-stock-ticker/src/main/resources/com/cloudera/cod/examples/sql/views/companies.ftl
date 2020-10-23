<#-- @ftlvariable name="" type="com.github.joshelser.views.CompaniesView" -->
<html>
    <body>
        <h1>Cloudera Operational Database</h1>
        <h3>Companies</h3>
        <#list companies as company>
          <p>Name: ${company.companyName}, Symbol: ${company.tickerName}, <a href="/company/${company.id}">Company Details</a>,
            <a href="/value/${company.id}">Latest Prices</a></p>
        </#list>
    </body>
</html>