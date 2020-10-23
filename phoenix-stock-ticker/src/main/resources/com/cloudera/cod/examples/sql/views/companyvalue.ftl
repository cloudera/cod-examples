<#-- @ftlvariable name="" type="com.github.joshelser.views.CompanyValueView" -->
<html>
    <body>
        <h1>Cloudera Operational Database</h1>
        <p>Name: ${company.companyName}, Symbol: ${company.tickerName}</p>
        <ul>
        <#list values as value>
          <li>${value.instant?number_to_datetime} => &#36;${value.price}</li>
        </#list>
        </ul>
        <p><a href="/">All Companies</a></p>
    </body>
</html>