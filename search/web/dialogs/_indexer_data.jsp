<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="com.google.inject.Injector"%>

<%@ include file="../inc/initVars.jsp" %>
<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="order" value="${param.sort}" />
<c:if test="${empty param.sort}">
    <c:set var="order" value="title" />
</c:if>
<c:set var="order_dir" value="${param.sort_dir}" />
<c:if test="${empty param.sort_dir}">
    <c:set var="order_dir" value="asc" />
</c:if>
<table cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td valign="top"><div  class="indexer_models">
                <table width="100%" cellpadding="0" cellspacing="0" border="0" id="indexerModels"><tr><td colspan="3">Top level models:</td></tr>
    <%
    //String[] models = kconfig.getProperty("fedora.topLevelModels").split(",");
    String[] models = kconfig.getPropertyList("fedora.topLevelModels");
    String selectedModel = request.getParameter("model");
    if(selectedModel==null || selectedModel.length()==0){
        selectedModel = models[0];
    }
    pageContext.setAttribute("selModel", selectedModel);
    for(String model:models){
        String css = "";
        if(model.equals(selectedModel)) css = "class=\"indexer_selected\"";
    %>
    <tr <%=css%>><td><a href="javascript:loadFedoraDocuments('<%=model%>', 0, '<c:out value="${order}" />', '<c:out value="${order_dir}" />');"><%=model%></a></td>
    <td valign="middle" width="20"><a href="javascript:loadFedoraDocuments('<%=model%>', 0, '<c:out value="${order}" />', '<c:out value="${order_dir}" />' );" title="select model"><img src="img/filter.png" border="0" /></a></td>
    <td valign="middle" width="20"><a href="javascript:indexModel('<%=model%>');" title="index model"><img src="img/admin/reindex.png" border="0" /></a></td>
    </tr>
    <%
    }
    %>
</table></div></td>
<td valign="top"><div class="indexer_selected">
<c:set var="rows" value="10" />

<c:url var="urlPage" value="${kconfig.fedoraHost}/risearch" >
    <c:param name="type" value="tuples" />
    <c:param name="flush" value="true" />
    <c:param name="lang" value="itql" />
    <c:param name="format" value="Sparql" />
    <c:param name="distinct" value="off" />
    <c:param name="stream" value="off" />
    <c:param name="query">
            select $object $title $date from <#ri> 
            where  $object <fedora-model:hasModel> <info:fedora/model:<c:out value="${selModel}" />> 
            and  $object <dc:title> $title 
            and  $object <fedora-view:lastModifiedDate> $date 
            order by $<c:out value="${order}" /> <c:out value="${order_dir}" />
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
            <x:param name="model" value="${selModel}" />
            <x:param name="sort" value="${order}" />
            <x:param name="sort_dir" value="${order_dir}" />
        </x:transform>
        
    </c:otherwise>
</c:choose>
</div></td></tr></table>