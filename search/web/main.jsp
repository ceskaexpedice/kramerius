<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<%//c:url var="url" value="http://localhost:8983/solr/select/select" 
//http://194.108.215.227:8080/solr/select?indent=on&version=2.2&q=fedora.model%3A%22info%3Afedora%2Fmodel%3Apage%22&start=0&rows=10&fl=*%2Cscore&qt=standard&wt=xslt&explainOther=&hl.fl=&facet=true&facet.field=fedora.model&tr=example.xsl
%> 
<c:url var="url" value="http://194.108.215.227:8080/solr/select/select" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
            <c:if test="${empty param.fq}">
                <c:param name="rows" value="0" />
            </c:if>
        </c:when>
        <c:when test="${param.q != null}" >
            <c:if test="${fn:containsIgnoreCase(param.q, '*')}" >
                <c:param name="qt" value="czparser" />
            </c:if>
            <c:param name="q" value="${param.q}" />
            <c:param name="rows" value="${param.rows}" />
        </c:when>
        
    </c:choose>
    <c:param name="facet.field" value="fedora.model" />
    <c:param name="f.fedora.model.facet.sort" value="false" />
    <c:param name="facet.field" value="abeceda" />
    <c:param name="f.abeceda.facet.sort" value="false" />
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
    </c:forEach>
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="${param.sort}" />
</c:url>

<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:if test="${exceptions != null}" >
    <c:import url="empty.xml" var="xml" />
</c:if>


<x:parse var="doc" xml="${xml}"  />
<c:set var="numDocs" scope="request" >
    <x:out select="$doc/response/result/@numFound" />
</c:set>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="header.jsp" %>
<body leftmargin="0" topmargin="0" onload="resizeDocFrame();" marginheight="0" marginwidth="0">
    <%@ include file="templates/logo.jsp" %>
<table>
   <tr valign='top'><td><%@ include file="resultsNav.jsp" %></td>
      <td><table>
            <tr><td><%@ include file="resultsMain.jsp" %></td></tr>
         </table>
      </td>
   </tr>
</table>
<table>
   <tr valign='top'>
   <td><%@ include file="templates/footer.jsp" %></td>
   </tr>
</table>
</body></html>
