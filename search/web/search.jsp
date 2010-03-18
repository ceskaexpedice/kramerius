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

<% 
out.clear(); 
%>
<%@ include file="inc/proccessFacets.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
    <body>
        <table style="width:100%"><tr><td align="center">
        <c:if test="${param.debug}" >
        <c:out value="${url}" />
        <br/>
        <c:out value="${exceptions}" />
        </c:if>
        <%@ include file="inc/searchForm.jsp" %>
        <table class="main usedFilters">
            <tr valign='top'>
                <td><%@ include file="inc/usedFilters.jsp" %></td>
            </tr>
        </table>
        <table class="main">
            <tr valign='top'>
                    <c:if test="${rows!='0'}">
                    <td class="leftMenu">
                            <% currentFacetName = "language"; %>
                            <%@ include file="inc/facet.jsp" %>
                            <% currentFacetName = "document_type"; %>
                            <%@ include file="inc/facet.jsp" %>
                            <% currentFacetName = "facet_autor"; %>
                            <%@ include file="inc/facet.jsp" %>
                    </td>
                    </c:if>
                <td>
                    <c:out value="${numDocs}" />
                    <%@ include file="inc/paginationPageNum.jsp" %>
                    <%//@ include file="inc/modelsTree.jsp" %>
                    
                     <c:choose>
                        <c:when test="${rows == 0}" >
                            <%@ include file="inc/suggest.jsp" %>
                            <table width="100%"><tr><td>
                            <% currentFacetName = "language"; %>
                            <%@ include file="inc/facet.jsp" %>
                            </td><td><% currentFacetName = "document_type"; %>
                            <%@ include file="inc/facet.jsp" %>
                            </td></tr></table>
                            <img src="img/intro.png" />
                        </c:when>
                        <c:otherwise >
                            <%@ include file="inc/resultsMain.jsp" %>
                        </c:otherwise>

                    </c:choose>
                </td>
                <td class="rightMenu">
                    <div class="facetTitle"><div><fmt:message key="Časová osa" />&nbsp;&nbsp;</div></div> 
                    <div class="facet">
                    <%@ include file="inc/dateAxisV.jsp" %>
                    </div>
                </td>
            </tr>
        </table>
        <table>
            <tr valign='top'>
                <td><%@ include file="templates/footer.jsp" %></td>
            </tr>
        </table>
        </td></tr></table>
</body></html>