<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%
    String numOpenedRows = kconfig.getProperty("search.results.numOpenedRows", "5");
    pageContext.setAttribute("numOpenedRows", numOpenedRows);
%>
<c:url var="xslPage" value="inc/results/xsl/results_main.xsl" />
<c:catch var="exceptions">
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2">
            <x:transform doc="${xml}"  xslt="${xsltPage}">
                <x:param name="bundle_url" value="${i18nServlet}"/>
                <x:param name="q" value="${param.q}"/>
                <x:param name="numOpenedRows" value="${numOpenedRows}"/>
                <x:param name="fqs"><c:forEach var="fqs" items="${paramValues.fq}">&fq=<c:out value="${fqs}" escapeXml="false" /></c:forEach>
                </x:param>
            </x:transform>
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>