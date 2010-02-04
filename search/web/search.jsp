<%@ page contentType="text/html" pageEncoding="UTF-8" %>
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
<%@ include file="inc/searchParams.jsp" %>

<% out.clear(); %>
<%@ include file="inc/proccessFacets.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
    <body >
        <c:if test="${param.debug}" >
            <c:out value="${url}" />
            <br/>
            <c:out value="${exceptions}" />
        </c:if>
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
        <tbody><tr><td>
        <%@ include file="templates/logo.jsp" %>
        <%@ include file="inc/searchForm.jsp" %>
        </td></tr></tbody></table>
        <table>
            <tr valign='top'>
                <td><%@ include file="usedFilters.jsp" %></td>
            </tr>
        </table>
        <table class="main">
            <tr valign='top'>
                <td class="leftMenu">
                    <%@ include file="inc/dateAxisV.jsp" %>
                    <% currentFacetName = "language"; %>
                    <%@ include file="inc/facet.jsp" %>
                </td>
                <td>
                    <c:out value="${numDocs}" />
                    <%@ include file="inc/paginationPageNum.jsp" %>
                    <%@ include file="inc/resultsMain.jsp" %>
                    <%@ include file="inc/modelsTree.jsp" %>
                </td>
            </tr>
        </table>
        <table>
            <tr valign='top'>
                <td><%@ include file="templates/footer.jsp" %></td>
            </tr>
        </table>
</body></html>