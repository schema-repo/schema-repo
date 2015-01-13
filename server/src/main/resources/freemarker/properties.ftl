<h2>${title}:</h2>
<table border="1">
    <#list props?keys as key>
        <tr>
            <th>${key}</th>
            <td><pre>${props[key]}</pre></td>
        </tr>
    </#list>
</table>
