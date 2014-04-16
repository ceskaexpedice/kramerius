<%@ page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="cz.incad.kramerius.utils.UTFSort"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            String t = request.getParameter("t");
            UTFSort utf_sort = new UTFSort();
            utf_sort.init();

            String term = utf_sort.translate(t);
            String including = request.getParameter("i");
            if (including==null){
                including = "true";
            }
            //term = java.net.URLEncoder.encode(term, "UTF-8");
            pageContext.setAttribute("term", term);
            pageContext.setAttribute("including", including);
%>
<c:choose>
    <c:when test="${param.field == 'browse_title'}">
        <c:set var="q" value="${param.field}:[\"${term}  *\" TO *]"/>
        <c:if test="${including == 'true'}"><c:set var="q" value="${q} OR (${param.field}:\"${term}  *\") OR (${param.field}:${term}  *)"/></c:if>
        
        <c:url var="url" value="${kconfig.solrHost}/select" >
            <c:param name="q" value="${q}" />
            <c:param name="facet.field" value="${param.field}" />
            <c:param name="f.${param.field}.facet.sort" value="false" />
            <c:param name="facet.mincount" value="1" />
            <c:param name="facet.limit" value="50" />
            <c:param name="rows" value="0" />
            <c:param name="facet" value="true" />
        </c:url>
    </c:when>
    <c:otherwise>
        <c:url var="url" value="${kconfig.solrHost}/terms" >
            <c:param name="terms.fl" value="${param.field}" />
            <c:param name="terms.lower.incl" value="${including}" />
            <c:param name="terms.sort" value="index" />
            <c:param name="terms.limit" value="50" />
            <c:param name="terms.lower" value="${term}" />
        </c:url>
    </c:otherwise>
</c:choose>

<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<c:url var="xslPage" value="inc/home/xsl/autocomplete.xsl" />
<c:catch var="exceptions">
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
    <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="incl" value="${including}" />
    </x:transform>
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
    </c:when>
</c:choose>