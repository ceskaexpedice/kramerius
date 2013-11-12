<%@ page pageEncoding="UTF-8" %>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<%@ page isELIgnored="false"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<scrd:securedContent action="reindex" sendForbidden="true">

<view:kconfig var="fedoraHost" key="fedoraHost" />
<c:catch var="ex">
<c:url var="url" value="${fedoraHost}/objects" >
    <c:param name="query" >title~'${param.s}'</c:param>
    <c:param name="resultFormat" value="xml" />
    <c:param name="pid" value="true" />
    <c:param name="title" value="true" />
    <c:param name="mDate" value="true" />
    <c:param name="type" value="true" />
    <c:param name="maxResults" value="${param.rows}" />
</c:url>
    <c:import url="${url}" var="doc" charEncoding="UTF-8"  />
<c:import url="indexer_search.xsl" var="xsltPage" charEncoding="UTF-8"  />
<x:transform doc="${doc}"  xslt="${xsltPage}"  >
    <x:param name="bundle_url" value="${i18nServlet}"/>
    <x:param name="rows" value="${param.rows}" />
    <x:param name="offset" value="${param.offset}" />
</x:transform>
</c:catch>
<c:choose>
    <c:when test="${ex!=null}">
        <tr><td>Error: ${ex}</td></tr>
    </c:when>
</c:choose>
</scrd:securedContent>