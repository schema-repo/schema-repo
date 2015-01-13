<#if schemas?has_content>
<table border="1">
    <tr><th>ID</th><th>Schema</th></tr>
    <#list schemas as schemaEntry>
        <tr>
            <td><h2><a href="id/${schemaEntry.id}">${schemaEntry.id}</a></h2></td>
            <td><pre>${schemaEntry.schema}</pre></td>
        </tr>
    </#list>
</table>
<#else>
<h2>No schemas have been registered yet</h2>
</#if>