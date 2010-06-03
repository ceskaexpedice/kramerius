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
        
        <div><c:if test="${display == 'none'}">
                    <xsl:attribute name="onclick" >javascript:showMainContent('<xsl:value-of select="$pid"/>', 'monograph')</xsl:attribute></c:if><span valign="top">*</span>
            <span>
                <fmt:message bundle="${lctx}">Hlavní název</fmt:message>:<br/>
                <div class="resultValue"><xsl:value-of select="mods:titleInfo/mods:title" /></div>
            </span>
            
        </div>
        <div id="moreDetails"><xsl:attribute name="style">display:<c:out value="${param.display}" />;</xsl:attribute>
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
                    <fmt:message bundle="${lctx}">monograph</fmt:message>
                </div>
            </span>
        </div>
        <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Název vydavatele</fmt:message>:<br/>
                    <xsl:if test="./mods:publisher">
                        <div class="resultValue">
                            <xsl:value-of select="./mods:publisher" />
                        </div>
                    </xsl:if>
                    <fmt:message bundle="${lctx}">Datum vydání</fmt:message>:<br/>
                    <xsl:if test="./mods:dateIssued">
                        <div class="resultValue">
                            <xsl:value-of select="./mods:dateIssued" />
                        </div>
                    </xsl:if>
                    <fmt:message bundle="${lctx}">Místo vydání</fmt:message>:<br/>
                    <xsl:if test="./mods:place/mods:placeTerm">
                        <div class="resultValue">
                            <xsl:value-of select="./mods:place/mods:placeTerm" />
                        </div>
                    </xsl:if>
                </span>
            </div>
        </xsl:for-each>
        <xsl:if test="mods:originInfo[@transliteration='printer']">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Název tiskaře</fmt:message>:<br/> 
                    <div class="resultValue">
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" />
                    </div>
                    <br/> 
                    <fmt:message bundle="${lctx}">Místo tisku</fmt:message>:<br/>
                    <div class="resultValue"> 
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:extent">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Fyzický popis</fmt:message>:<br/>
                    
                    <xsl:if test="contains(mods:physicalDescription/mods:extent, ',')">
                        <fmt:message bundle="${lctx}">Rozměry</fmt:message>:<br/> 
                        <div class="resultValue"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" />
                        </div>
                        <br/>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="contains(mods:physicalDescription/mods:extent, ',')">
                            <fmt:message bundle="${lctx}">Rozměry</fmt:message>:<br/> 
                            <div class="resultValue"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" />
                            </div>
                            <br/>
                            <fmt:message bundle="${lctx}">Rozsah</fmt:message>:<br/> 
                            <div class="resultValue"><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" />
                            </div>
                        </xsl:when>
                        <xsl:otherwise>
                            <fmt:message bundle="${lctx}">Rozsah</fmt:message>:<br/> 
                            <div class="resultValue"><xsl:value-of select="mods:physicalDescription/mods:extent" /> 
                            </div>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Stav z hlediska ochrany fondů</fmt:message>:<br/>
                    <div class="resultValue">
                        <fmt:message bundle="${lctx}">Aktuální stav</fmt:message>:<br/> 
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                    </div>
                </span>
            </div>
        </xsl:if>
        
        <xsl:if test="mods:location/mods:physicalLocation">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Místo uložení</fmt:message>:<br/>
                    <div class="resultValue">
                        <xsl:value-of select="mods:location/mods:physicalLocation" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:location/mods:shelfLocator">
            <div><span valign="top">*</span>
                <span>
                    <fmt:message bundle="${lctx}">Signatura</fmt:message>:<br/>
                    <div class="resultValue">
                        <xsl:value-of select="mods:location/mods:shelfLocator" />
                    </div>
                </span>
            </div>
        </xsl:if>
        </div>
        
    </xsl:template>
</xsl:stylesheet>
