<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@ include file="inc/searchParams.jsp" %>
<jsp:useBean id="xml" type="java.lang.String" />
<%
    XSLService xs = (XSLService) ctxInj.getInstance(XSLService.class);
    try {
        String xsl = "resultsMain1.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl);
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/proccessFacets.jsp" %>
    <%@ include file="inc/html_header.jsp" %>
    <body>
        <!--  procesy - dialogy -->
        <%@ include file="dialogs/_processes_dialogs.jsp" %>
        <table style="width:100%"><tr><td align="center">
                    <c:if test="${param.debug}" >
                        <c:out value="${url}" />
                        <br/>
                        <c:out value="${exceptions}" />
                    </c:if>
                        <form name="searchForm" id="searchForm" method="GET" action="./" onsubmit="return checkQuery()">
                    <%@ include file="inc/searchForm.jsp" %>
                    <c:if test="${rows!='0'}"><%@ include file="inc/usedFilters.jsp" %></c:if>
                     </form>
                    <table class="main">
                        <tr valign='top'>
                            <c:if test="${rows!='0'}">
                                <td class="leftMenu">
                                    <% currentFacetName = "dostupnost";%>
                                    <%@ include file="inc/facet.jsp" %>
                                    <% currentFacetName = "document_type";%>
                                    <%@ include file="inc/facet.jsp" %>
                                    <% currentFacetName = "facet_autor";%>
                                    <%@ include file="inc/facet.jsp" %>
                                    <% currentFacetName = "language";%>
                                    <%@ include file="inc/facet.jsp" %>
                                </td>
                        
                            </c:if>
                            <td class="centralCell">
                                <c:choose>
                                    <c:when test="${rows == 0}" >
                                        <table width="100%" cellspacing="0" cellpadding="0"><tr><td valign="top"><%@ include file="inc/suggest.jsp" %>
                                                </td><td valign="top"><% currentFacetName = "document_type";%>
                                                    <%@ include file="inc/facet.jsp" %>
                                                    <script type="text/javascript" language="javascript">
                                                        $(document).ready(function(){
                                                            $(".facetItem").removeClass('moreFacets');
                                                            $("#suggestBody").css('height', $(".facetBody").height()-5);
                                                        });
                                                    </script>
                                                </td></tr></table>
                                                <%@ include file="inc/intro.jsp" %>
                                            </c:when>
                                            <c:otherwise >
                                        <div id="resultsDiv" class="ui-tabs ui-widget ui-widget-content ui-corner-all shadow10" >
                                            <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" style="padding:0 0.1em 0 0;">
                                                <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active " style="width:100%;">
                                                    <a class="box">&#160;<c:out value="${numDocs}" />&#160;<c:out value="${numDocsStr}" /></a>
                                                    <c:if test="${numDocs>1}">
                                                        <%@ include file="inc/paginationPageNum.jsp" %>
                                                        <%@ include file="inc/sort.jsp" %>
                                                    </c:if>
                                                </li>
                                            </ul>
                                            <div id="resultsBody" class="ui-tabs-panel ui-widget-content ui-corner-bottom">
                                                <%@ include file="inc/resultsMain.jsp" %>
                                            </div>
                                        </div>
                                    </c:otherwise>

                                </c:choose>
                            </td>
                            <td id="rightMenu" class="rightMenu">
                                <div id="timeLineDiv" class="ui-tabs ui-widget ui-widget-content ui-corner-all shadow10" >
                                    <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" style="padding:0 0.1em 0 0;">
                                        <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active " style="width:100%;"><a class="box">
                                                <fmt:message bundle="${lctx}" key="Časová osa" /></a></li>
                                    </ul>
                                    <div id="timeLineBody" class="ui-tabs-panel ui-corner-bottom">
                                        <%@ include file="inc/dateAxisV.jsp" %>
                                    </div>
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
   </body>
</html>