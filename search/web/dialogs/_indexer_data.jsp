<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%@ include file="../inc/initVars.jsp" %>
<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<table><tr><td valign="top"><div id="indexerModels" style="width:150px;border-right:1px solid silver;"><div>Top level models:</div>
    <%
    String[] models = kconfig.getProperty("fedora.topLevelModels").split(",");
    for(String model:models){
    %>
    <div><a href="javascript:loadFedoraDocuments('<%=model%>', 0);"><%=model%></a> <a href="javascript:indexModel('<%=model%>');">index</a></div>
    <%
    }
    %>
</div></td>
<td valign="top">
<c:set var="rows" value="5" />
<c:url var="urlPage" value="${kconfig.fedoraHost}/risearch" >
    <c:param name="type" value="tuples" />
    <c:param name="flush" value="true" />
    <c:param name="lang" value="itql" />
    <c:param name="format" value="Sparql" />
    <c:param name="distinct" value="off" />
    <c:param name="stream" value="off" />
    <c:param name="query">
            select $object $title from <#ri> 
            where  $object <fedora-model:hasModel> <info:fedora/model:<c:out value="${param.model}" />> 
            and  $object <dc:title> $title 
            order by $title
            limit <c:out value="${rows}" />
            offset <c:out value="${param.offset}" />
    </c:param>
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${urlPage}" var="xml" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}" >
        <jsp:useBean id="exceptions" type="java.lang.Exception" />
        <% System.out.println(exceptions); %>
    </c:when>
    <c:otherwise>
        <%--
        <x:forEach varStatus="status" select="$doc//*">
            <x:set var="name" select="name()"/>
            <x:if select="$name='result'">
                <x:out select="./text()"/><br/>
            </x:if>
            <br/>
            
        </x:forEach>
        <c:out value="${xml}" />
        --%>
        <x:parse var="doc" xml="${xml}"  />
        <c:import url="indexer.xsl" var="xsltPage" charEncoding="UTF-8"  />
        <x:transform doc="${xml}"  xslt="${xsltPage}"  >
            <x:param name="rows" value="${rows}" />
            <x:param name="offset" value="${param.offset}" />
            <x:param name="model" value="${param.model}" />
        </x:transform>
        
    </c:otherwise>
</c:choose>
</td></tr></table>