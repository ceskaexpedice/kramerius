<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0" 
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                 xmlns:mods="http://www.loc.gov/mods/v3" 
                 xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"  >
    <xsl:output method="html" indent="yes" encoding="UTF-8" />
    <%@ page pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
    <%@ page isELIgnored="false"%>
    <c:choose>
        <c:when test="${param.language != null}" >
            <fmt:setLocale value="${param.language}" />
        </c:when>
    </c:choose>
    <fmt:setBundle basename="labels" />
    <fmt:setBundle basename="labels" var="bundleVar" />
    
    <!-- TODO customize transformation rules 
    syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="volumePid">uuid:<xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
        <xsl:variable name="volumeNumber"><xsl:value-of select="mods:part[@type = 'PeriodicalSupplement']/mods:detail[@type = 'issue']/mods:number" /></xsl:variable>
        <xsl:variable name="link">javascript:openItem('<xsl:value-of select="$volumePid" />', '<c:out value="${param.title}" />');</xsl:variable>
        
        <!--Datum vydání ročníku: 1861	 Číslo ročníku: 1	(1/462)-->
            a@<xsl:value-of select="$volumeNumber" />@<fmt:message>Datum vydání výtisku</fmt:message> <a>
                <xsl:attribute name="href"><xsl:value-of select="$link" />
                </xsl:attribute><xsl:value-of select="mods:part/mods:date" /></a>
                <fmt:message>Číslo výtisku</fmt:message> <a >
                <xsl:attribute name="href"><xsl:value-of select="$link" />
                </xsl:attribute><xsl:value-of select="$volumeNumber" />
                 </a> <fmt:message>příloha</fmt:message>
        
    </xsl:template>
</xsl:stylesheet>
