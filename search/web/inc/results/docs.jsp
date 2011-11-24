<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>

<%
    try {
        String xsl = "results_main.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:catch var="exceptions">
    <c:url var="results" value="inc/results/xsl/results_main.xsl" />
    <c:import url="${results}" var="resultsxsl" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${resultsxsl}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
            <x:transform doc="${xml}"  xslt="${resultsxsl}">
                <x:param name="bundle_url" value="${i18nServlet}"/>
                <x:param name="q" value="${param.q}"/>
                <c:if test="${!empty param.collection}">
                    <x:param name="collection" value="&collection=${param.collection}" />
                </c:if>
                <x:param name="fqs"><c:forEach var="fqs" items="${paramValues.fq}">&fq=<c:out value="${fqs}" escapeXml="false" /></c:forEach></x:param>
            </x:transform>
    </c:otherwise>
</c:choose>