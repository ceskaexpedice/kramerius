<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:set var="PIDquerys" scope="request"></c:set>
<c:set var="PIDs" scope="request"></c:set>
<c:set var="title_origs" scope="request"></c:set>
<div id="results_main">
    <x:forEach varStatus="status" select="$doc/response/result/doc">
    
        <div id="result" class="r<c:out value="${status.count % 2}" />">
        <c:set var="uuid" >
            <x:out select="./str[@name='PID']"/>
        </c:set>
        <c:set var="uuidSimple" >
            <x:out select="substring-after(./str[@name='PID'], 'uuid:')"/>
        </c:set>
            
            <c:set var="urlBiblioMods" >
                <c:out value="${fedoraHost}" />/get/<c:out value="${uuid}" />/BIBLIO_MODS
            </c:set>
            <c:set var="urlSolr" >
                <c:out value="${fedoraSolr}" />?q=PID:"<c:out value="${uuid}" />"
            </c:set>
            <c:set var="urlReindex" >
                IndexModel?full=true&model=<x:out select="substring(./str[@name='fedora.model'], 19)" />&pid=<c:out value="${uuid}" />
            </c:set>
    <x:choose>
        
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:monograph'">
            <%@ include file="results/monograph.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:monographunit'">
            <%@ include file="results/monographunit.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:page'">
            <%@ include file="results/page.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodical'">
            <%@ include file="results/periodical.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodicalvolume'">
            <%@ include file="results/periodicalvolume.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodicalitem'">
            <%@ include file="results/periodicalitem.jsp" %>
        </x:when>
        <x:otherwise>
            <%@ include file="results/default.jsp" %>
        </x:otherwise>
    </x:choose>
    <a href='<c:out value="${urlBiblioMods}" />' target="fedora">biblio_mods</a> 
    <a href='<c:out value="${urlSolr}" />' target="solr">solr</a>
    <a href='<c:out value="${urlReindex}" />' target="reindex">reindex</a>
    </div>
</x:forEach>
</div>

<script type="text/javascript"> 
        $(document).ready(function(){
                getTotalPagesInResults();
        });
</script>