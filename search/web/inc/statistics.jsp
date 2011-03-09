<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%
            //Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            //LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
            //pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);


%>

<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >
        root_pid:"${param.pid}" and 
    </c:param>
    <c:param name="rows" value="0" />
    <c:param name="facet.field" value="document_type" />
    <c:param name="facet.mincount" value="1" />
    <c:param name="facet" value="true" />
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />
<%
cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) inj.getInstance(cz.incad.kramerius.service.XSLService.class);
    try {
        String xsl = "statistics.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl);
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:set var="xsltPage" >
    
<xsl:stylesheet  version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:template match="/">
        <xsl:for-each select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst">
            <xsl:for-each select="./int" >
                <xsl:variable name="model">document.type.<xsl:value-of select="@name"/></xsl:variable>
                <div><xsl:value-of select="$bundle/value[@key=$model]"/>&#160;(<xsl:value-of select="."/>)</div>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
</c:set>
<c:catch var="exceptions">
    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
        <x:param name="bundle_url" value="${i18nServlet}"/>
    </x:transform>
    <c:set var="obj" value="#tabs_${param.level}" />
    <c:set var="href" value="#{href}" />
    <c:set var="label" value="#{label}" />
    <c:set var="target" value="#tab${label}-page" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
</c:choose>

