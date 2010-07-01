<?xml version="1.0" encoding="UTF-8"?><%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
	LocalizationContext lctx= ctxInj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);
%>

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
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />
    <!-- TODO customize transformation rules 
    syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:param name="pid" select="pid"/>
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="uuid" ><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
        <c:if test="${display != 'full'}">
        <div>
            <xsl:attribute name="title" ><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:attribute>
            <div class="resultValue"><xsl:value-of select="mods:titleInfo/mods:title" /></div>
        </div>
        </c:if>
        <c:if test="${display == 'full'}"><table class="detailsTable">
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Hlavní název</fmt:message></td>
                <td class="resultValue"><xsl:value-of select="mods:titleInfo/mods:title" /></td>
            </tr>
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Autor</fmt:message></td>
                <td class="resultValue"><xsl:for-each select="mods:name[@type='personal']">
                    <xsl:if test="./mods:role/mods:roleTerm = 'Author'">
                        <div>
                            <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                            <xsl:value-of select="./mods:namePart[@type='given']" />
                        </div>
                    </xsl:if>
                </xsl:for-each></td>
            </tr>
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Druh dokumentu</fmt:message></td>
                <td class="resultValue"><fmt:message bundle="${lctx}">monograph</fmt:message></td>
            </tr>
            
            <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
            <tr>                    
                <td class="detailLabel"><fmt:message bundle="${lctx}">Název vydavatele</fmt:message></td>
                <td class="resultValue"><xsl:if test="./mods:publisher">
                     <xsl:value-of select="./mods:publisher" />
                </xsl:if></td>
            </tr>
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Datum vydání</fmt:message></td>
                <td class="resultValue"><xsl:if test="./mods:dateIssued">
                      <xsl:value-of select="./mods:dateIssued" />
                </xsl:if></td>
            </tr>
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Místo vydání</fmt:message></td>
                <td class="resultValue"><xsl:if test="./mods:place/mods:placeTerm">
                    <xsl:value-of select="./mods:place/mods:placeTerm" />
                    </xsl:if></td>
            </tr>
            </xsl:for-each>
            <xsl:if test="mods:originInfo[@transliteration='printer']">
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Název tiskaře</fmt:message></td>
                <td class="resultValue"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" /></td>
            </tr>
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Místo tisku</fmt:message></td>
                <td class="resultValue"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" /></td>
            </tr>
            </xsl:if>
            
            <xsl:if test="mods:physicalDescription/mods:extent">
            <tr>
                <td class="detailLabelGroup" colspan="2"><fmt:message bundle="${lctx}">Fyzický popis</fmt:message>
                <table class="detailsTable"><xsl:choose>
                <xsl:when test="contains(mods:physicalDescription/mods:extent, ',')">
                    <tr>
                        <td class="detailLabel"><fmt:message bundle="${lctx}">Rozměry</fmt:message></td>
                        <td class="resultValue"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></td>
                    </tr>
                    <tr>
                        <td class="detailLabel"><fmt:message bundle="${lctx}">Rozsah</fmt:message></td>
                        <td class="resultValue"><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></td>
                    </tr>
                </xsl:when>
                <xsl:otherwise>
                    <tr>
                        <td class="detailLabel"><fmt:message bundle="${lctx}">Rozsah</fmt:message></td>
                        <td class="resultValue"><xsl:value-of select="mods:physicalDescription/mods:extent" /></td>
                    </tr>
                </xsl:otherwise>
            </xsl:choose></table></td>
                
            </tr>
            
            </xsl:if>
            <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Stav z hlediska ochrany fondů</fmt:message></td>
                <td class="resultValue">
                        <fmt:message bundle="${lctx}">Aktuální stav</fmt:message>: 
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                </td>
            </tr>
            </xsl:if>
            <xsl:if test="mods:location/mods:physicalLocation">
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Místo uložení</fmt:message></td>
                <td class="resultValue"><xsl:value-of select="mods:location/mods:physicalLocation" /></td>
            </tr>
            </xsl:if>
            <xsl:if test="mods:location/mods:shelfLocator">
            <tr>
                <td class="detailLabel"><fmt:message bundle="${lctx}">Signatura</fmt:message></td>
                <td class="resultValue"><xsl:value-of select="mods:location/mods:shelfLocator" /></td>
            </tr>
            </xsl:if>
            
        </table></c:if>
        
        
        
    </xsl:template>
</xsl:stylesheet>
