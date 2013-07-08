<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%
    Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
    KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
    pageContext.setAttribute("kconfig", kconfig);
    
%>
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" value="root_pid:\"${param.root}\"" />
    <c:param name="facet.field" value="dostupnost" />
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
    <c:param name="rows" value="0" />
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <%--x:parse var="doc" xml="${xml}"  /--%>
    <c:set var="xsl">
        <?xml version="1.0"?>
        <xsl:stylesheet
          xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">
            <xsl:output  method="html"  />
          <xsl:template match="/">
            <xsl:choose>
                <xsl:when test="count(/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int)=2" >0</xsl:when>
                <xsl:when test="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int/@name='public'" >1</xsl:when>
                <xsl:when test="/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int/@name='private'" >2</xsl:when>
            </xsl:choose>
          </xsl:template>
        </xsl:stylesheet>

      </c:set>
    <x:transform xml="${xml}" xslt="${xsl}"/>
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
</c:choose>
