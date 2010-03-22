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
<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" value="PID:\"${param.pid}\"" />
</c:url>

<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:if test="${exceptions != null}" >
    <c:import url="empty.xml" var="xml" />
</c:if>

<%--
<%
	String url = (String)pageContext.getAttribute("url");
	log(url);
	InputStream is = RESTHelper.inputStream(url,kconfig.getFedoraUser(), kconfig.getFedoraPass());
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	IOUtils.copyStreams(is, bos);
	byte[] bytes= bos.toByteArray();
	String str = new String(bytes,"UTF-8");
	log(str);
	System.out.println("TEST >>> "+str);
%>

<c:set var="xml"><%=str%></c:set>
<x:parse doc="${xml}" var="doc"/>
--%>

<x:parse var="doc" xml="${xml}"  />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%><html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
    <%@ include file="inc/html_header.jsp" %>
    <body >
        <table style="width:100%"><tr><td align="center">
        <c:if test="${param.debug}" >
        <c:out value="${url}" />
        <br/>
        <c:out value="${exceptions}" />
        </c:if>
        <%@ include file="inc/searchForm.jsp" %>
        <table>
            <tr valign='top'>
                <td><%//@ include file="usedFilters.jsp" %></td>
            </tr>
        </table>
        <table class="main">
            <tr valign='top'>
                <td>
                    <%//@ include file="item_1.jsp" %>
                    <div id="mainContent"></div>
               	    <%//@ include file="gwtView.jsp" %>
                </td>
                <td class="itemMenu">
                    <div id="itemTree">
                    <%@ include file="inc/details/itemMenu.jsp" %>
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