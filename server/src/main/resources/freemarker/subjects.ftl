<#if subjects?has_content>
<script language="JavaScript">
    path = window.location.pathname;
    if (path.charAt(path.length-1) != '/') {
        window.location.assign(window.location.href + "/");
    }
</script>
<h2>Registered Subjects:</h2>
<table>
    <#list subjects as subject>
        <tr>
            <td><h2><pre><a href="${subject.name}/all">${subject.name}</a></pre></h2></td>
            <td>(<a href="${subject.name}/config">config</a>)</td>
            <td><a href="${subject.name}/latest">Latest Schema</a></td>
        </tr>
    </#list>
</table>
<#else>
<h2>Repository does not have any subjects registered yet.</h2>
</#if>