<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>
<%@page import="java.util.Map,java.util.HashMap"%>

<view:kconfig var="policyPublic" key="search.policy.public" defaultValue="false" />
<%
    try {
        
        String xsl = "grouped_results.xsl";
        
        boolean isCollapsed = Boolean.parseBoolean(request.getAttribute("isCollapsed").toString());
        if(!isCollapsed){
            xsl = "not_grouped_results.xsl";
        }
        if (xs.isAvailable(xsl)) {
            Map<String, String> params = new HashMap<String, String>();
            if(request.getParameter("q")!=null){
                params.put("q", request.getParameter("q"));
            }
            if(request.getParameter("collection")!=null){
                params.put("collection", "&collection=" + request.getParameter("collection"));
            }
            if(request.getParameter("policyPublic")!=null){
                params.put("policyPublic", request.getParameter("policyPublic"));
            }
            String text = xs.transform(xml, xsl, lctx.getLocale(), params);
            out.println(text);
        }else{
%>
<c:catch var="exceptions">
    <c:choose>
        <c:when test="${isCollapsed}"><c:url var="results" value="inc/results/xsl/grouped_results.xsl" /></c:when>
        <c:otherwise><c:url var="results" value="inc/results/xsl/not_grouped_results.xsl" /></c:otherwise>
    </c:choose>
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
                <x:param name="policyPublic" value="${policyPublic}"/>
            </x:transform>
    </c:otherwise>
</c:choose>
                
<%
        }
    } catch (Exception e) {
        out.println(e);
    }
%>