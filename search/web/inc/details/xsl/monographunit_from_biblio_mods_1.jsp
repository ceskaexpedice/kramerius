<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0" 
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                 xmlns:mods="http://www.loc.gov/mods/v3" 
                 xmlns:fmt="http://java.sun.com/jsp/jstl/fmt" >
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />
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
        <xsl:variable name="unitPid">uuid:<xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
            <a>
                <xsl:attribute name="href">javascript:openUnit('<xsl:value-of select="$unitPid" />', this);</xsl:attribute>
                <xsl:attribute name="href">javascript:browseInTree('<xsl:value-of select="$unitPid" />', this);</xsl:attribute>
                <fmt:message>Volume</fmt:message> - <xsl:value-of select="mods:titleInfo/mods:title" />
            <xsl:if test="mods:part/mods:detail/mods:title != ''" > (<xsl:value-of select="mods:part/mods:detail/mods:title" />)</xsl:if> - <xsl:value-of select="mods:part/mods:detail/mods:number" /></a>
        
    </xsl:template>
</xsl:stylesheet>
