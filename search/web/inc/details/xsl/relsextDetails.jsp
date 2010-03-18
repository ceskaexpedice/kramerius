<%@ page pageEncoding="UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0" 
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                 xmlns:mods="http://www.loc.gov/mods/v3" 
                 xmlns:fmt="http://java.sun.com/jsp/jstl/fmt" >
    <xsl:output method="html" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
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
    
    <xsl:param name="pid" select="pid"/>
    <xsl:template match="/">
        <xsl:value-of select="$pid" />&#160;<br/>
        <xsl:for-each select="//doc">
            
            <xsl:if test="not(preceding-sibling::*[1]/str[@name='fedora.model'] = ./str[@name='fedora.model']/text())">
                <div class="relList">
                <xsl:value-of select="./str[@name='fedora.model']" />
                </div>
            </xsl:if>
            <div>
                <xsl:attribute name="id"><xsl:value-of select="./str[@name='PID']" /></xsl:attribute>
                <xsl:for-each select="./arr[@name='details']/str">
                    <xsl:value-of select="." />&#160;
                </xsl:for-each>
            </div>
            
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
