<?xml version="1.0" encoding="UTF-8"?><%@ page pageEncoding="UTF-8" %>
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
<c:choose>
    <c:when test="${param.display != null}">
        <c:set var="display" value="${param.display}"/>
    </c:when>
    <c:otherwise>
        <c:set var="display" value="block"/>
    </c:otherwise>
</c:choose>
<xsl:stylesheet  version="1.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:mods="http://www.loc.gov/mods/v3"
    exclude-result-prefixes="mods" >
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />
    <!-- TODO customize transformation rules 
    syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="uuid" ><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
        <div><span valign="top">*</span>
            <span>
                <fmt:message bundle="${lctx}">Hlavní název</fmt:message>:<br/>
                <div class="resultValue"><a>
                        <xsl:attribute name="href">./item.jsp?pid=uuid:<xsl:value-of select="$uuid"/></xsl:attribute><xsl:value-of select="mods:titleInfo/mods:title" /></a></div>
            </span>
            <c:if test="${display == 'none'}"><a onclick="$('#moreDetails').toggle();" href="#">more</a></c:if>
        </div>
        <div id="moreDetails">
            <xsl:attribute name="style">display:<c:out value="${param.display}" />;</xsl:attribute>
        <div><span valign="top">*</span>
            <span>
                <fmt:message bundle="${lctx}">Autor</fmt:message>:<br/>
                <xsl:for-each select="mods:name[@type='personal']">
                    <xsl:if test="./mods:role/mods:roleTerm = 'Author'">
                        <div class="resultValue">
                            Příjmení: &#160;<xsl:value-of select="./mods:namePart[@type='family']" />&#160;&#160;
                            Jméno: &#160;<xsl:value-of select="./mods:namePart[@type='given']" />
                        </div>
                    </xsl:if>
                </xsl:for-each>
            </span>
        </div>
        <div><span valign="top">*</span>
            <span>
                <fmt:message bundle="${lctx}">Druh dokumentu</fmt:message>:<br/>
                <div class="resultValue">
                    <fmt:message bundle="${lctx}">info:fedora/model:monograph</fmt:message>
                </div>
            </span>
        </div>
        </div>
    </xsl:template>
</xsl:stylesheet>
