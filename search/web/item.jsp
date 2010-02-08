<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>
    <%@ include file="inc/initVars.jsp" %>
<c:set var="pageType" value="search" />
<jsp:useBean id="pageType" type="java.lang.String" />
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:url var="url" value="${kconfig.solrHost}" >
    <c:param name="q" value="PID:\"${param.pid}\"" />
    <c:param name="facet.field" value="fedora.model" />
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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="inc/proccessFacets.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
    <body >
        <c:if test="${param.debug}" >
            <c:out value="${url}" />
        </c:if>
        <%@ include file="templates/logo.jsp" %>
        <%@ include file="inc/searchForm.jsp" %>
        <table>
            <tr valign='top'>
                <td><%@ include file="usedFilters.jsp" %></td>
            </tr>
        </table>
        <table>
            <tr valign='top'>
                <td>
                    <%@ include file="inc/facets.jsp" %>
                </td>
                <td>
                    <%@ include file="item_1.jsp" %>
               		<%@ include file="gwtView.jsp" %>
                </td>
            </tr>
        </table>
        <table>
            <tr valign='top'>
                <td><%@ include file="templates/footer.jsp" %></td>
            </tr>
        </table>
</body></html>