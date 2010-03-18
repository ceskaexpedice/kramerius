<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0" 
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                 xmlns:mods="http://www.loc.gov/mods/v3" 
                 xmlns:fmt="http://java.sun.com/jsp/jstl/fmt" >
    <xsl:output method="plain" indent="no" encoding="UTF-8"  omit-xml-declaration="yes" />
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
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="pageType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
        <xsl:if test="mods:part">
                <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /><xsl:choose>
                    <xsl:when test="$pageType='Blank'">
                        (<fmt:message>Blank</fmt:message>)</xsl:when>
                    <xsl:when test="$pageType='TitlePage'">
                        (<fmt:message>TitlePage</fmt:message>)</xsl:when>
                    <xsl:when test="$pageType='TableOfContents'">
                        (<fmt:message>TableOfContents</fmt:message>)</xsl:when>
                </xsl:choose>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
