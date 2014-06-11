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
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            
%>
<c:url var="url" value="${kconfig.solrHost}/select/" >
    <c:param name="q" >
        *:*
    </c:param>
        
    
    <view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
    <c:if test="${cols.current != null}">
        <c:param name="fq" value="collection:\"${cols.current.pid}\"" />
    </c:if>
    <c:param name="rows" value="0" />
    <c:param name="facet.field" value="model_path" />
    <c:param name="facet.field" value="document_type" />
    <c:param name="facet.field" value="keywords" />
    <c:param name="f.keywords.facet.limit" value="30" />
    <c:param name="facet.field" value="language" />
    <c:param name="facet.field" value="dostupnost" />
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />
<%
cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);

    try {
        String xsl = "facets_home.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
        String numOpenedRows = kconfig.getProperty("search.results.numOpenedRows", "5");
        pageContext.setAttribute("numOpenedRows", numOpenedRows);
    } catch (Exception e) {
        out.println(e);
        out.println(xml);
    }
%>
<view:kconfig var="policyPublic" key="search.policy.public" defaultValue="false" />
<c:catch var="exceptions">
    <c:url var="facetxslurl" value="xsl/facets_home.xsl" />
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
                <x:param name="numOpenedRows" value="10"/>
                <x:param name="policyPublic" value="${policyPublic}"/>
            </x:transform>
    </c:otherwise>
</c:choose>