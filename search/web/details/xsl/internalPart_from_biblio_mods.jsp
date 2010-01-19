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
        <xsl:variable name="pageType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
            <a>
                <xsl:attribute name="href">javascript:openInternalPart('<xsl:value-of select="$unitPid" />', 'info:fedora/model:internalpart', '<xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />');</xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$pageType='Chapter'">
                        (<fmt:message>Chapter</fmt:message>)</xsl:when>
                    <xsl:when test="$pageType='Table'">
                        (<fmt:message>Table</fmt:message>)</xsl:when>
                    <xsl:when test="$pageType='Introduction'">
                        (<fmt:message>Introduction</fmt:message>)</xsl:when>
                </xsl:choose> - <xsl:value-of select="mods:titleInfo/mods:title" /> - 
                <xsl:value-of select="mods:part/mods:extent/mods:list" /></a>
        
    </xsl:template>
</xsl:stylesheet>
