<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%
    try {
        String xsl = "facets.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
        String numOpenedRows = kconfig.getProperty("search.results.numOpenedFacets", "8");
        pageContext.setAttribute("numOpenedRows", numOpenedRows);
    } catch (Exception e) {
        out.println(e);
        out.println(xml);
    }
%>
<view:kconfig var="policyPublic" key="search.policy.public" defaultValue="false" />
<c:catch var="exceptions">
    <c:url var="facetxslurl" value="inc/results/xsl/facets.xsl" />
    <c:import url="${facetxslurl}" var="facetxsl" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${facetxsl}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
            <x:transform doc="${xml}"  xslt="${facetxsl}">
                <x:param name="bundle_url" value="${i18nServlet}"/>
                <x:param name="numOpenedRows" value="${numOpenedRows}"/>
                <x:param name="policyPublic" value="${policyPublic}"/>
            </x:transform>
    </c:otherwise>
</c:choose>