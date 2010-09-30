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
            <xsl:choose>
                <xsl:when test="mods:titleInfo/mods:title">
                    <xsl:variable name="TITLE"><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:variable>
                </xsl:when>
                <xsl:when test="mods:titleInfo/mods:title">
                    <xsl:variable name="TITLE"><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:variable>
                </xsl:when>
            </xsl:choose>
            
            <div id="detailsShort"><ul>
            <xsl:attribute name="title" ><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:attribute>
            <xsl:if test="./mods:identifier[@type='issn']/text()">
            <li>
                <span class="label">ISSN: </span>
                <span class="value"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
            </li>
            </xsl:if>
            <li class="value"><xsl:value-of select="mods:titleInfo/mods:title" /></li>
            <xsl:if test="mods:part/mods:detail[@type = 'volume']/mods:number">
                
            <li>
            <fmt:message bundle="${lctx}">Datum vydání</fmt:message>
            <xsl:value-of select="mods:part/mods:date" />
            <fmt:message bundle="${lctx}">Číslo</fmt:message>
            <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
            </li>
            </xsl:if>
            
            <xsl:if test="mods:part[@type = 'PeriodicalIssue']">
            <li>
            <fmt:message bundle="${lctx}">Datum vydání</fmt:message>
            <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:date" />
            <fmt:message bundle="${lctx}">Číslo</fmt:message>
            <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:detail[@type = 'issue']/mods:number" />
            </li>
            </xsl:if>
            
            <xsl:variable name="partType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
            <!-- page -->
                <xsl:choose>
                    <xsl:when test="$partType='Blank'">
                        <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /> (<fmt:message bundle="${lctx}">Blank</fmt:message>)</xsl:when>
                    <xsl:when test="$partType='TitlePage'">
                        <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /> (<fmt:message bundle="${lctx}">TitlePage</fmt:message>)</xsl:when>
                    <xsl:when test="$partType='TableOfContents'">
                        <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /> (<fmt:message bundle="${lctx}">TableOfContents</fmt:message>)</xsl:when>
                    <xsl:when test="$partType='Index'">
                        <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /> (<fmt:message bundle="${lctx}">TableOfContents</fmt:message>)</xsl:when>
                    <xsl:when test="$partType='NormalPage'">
                        <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /></xsl:when>
                </xsl:choose>
            <!-- end page -->
            
            <!-- monograph unit -->
            <xsl:if test="mods:part[@type = 'Volume']/mods:detail">
            <fmt:message bundle="${lctx}">Volume</fmt:message>
            <xsl:if test="mods:part[@type = 'Volume']/mods:detail/mods:title != ''" >
                (<xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:title" />)
            </xsl:if> - <xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:number" />
            </xsl:if>
            <!-- end monograph unit -->
            
            <!-- internal part -->
                <xsl:choose>
                    <xsl:when test="$partType='Chapter'">
                        (<fmt:message bundle="${lctx}">Chapter</fmt:message>) - </xsl:when>
                    <xsl:when test="$partType='Table'">
                        (<fmt:message bundle="${lctx}">Table</fmt:message>) - </xsl:when>
                    <xsl:when test="$partType='Introduction'">
                        (<fmt:message bundle="${lctx}">Introduction</fmt:message>) - </xsl:when>
                </xsl:choose>
                <xsl:value-of select="mods:part/mods:extent/mods:list" />
            <!-- end internal part -->

        </ul></div>
        </c:if>
        <c:if test="${display == 'full'}"><div id="details">
            <ul>
            <li>
                <span class="label">ISSN</span>
                <span class="value"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
            </li>
            <li>
                <span class="label"><fmt:message bundle="${lctx}">filter.maintitle</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:titleInfo/mods:title" /></span>
            </li>
            <xsl:if test="mods:titleInfo/mods:subTitle/text()">
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Podnázev</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:titleInfo/mods:subTitle" /></span>
            </li>
            </xsl:if>
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Druh dokumentu</fmt:message></span>
                <span class="value"><fmt:message bundle="${lctx}"><c:out value="${param.model}"/></fmt:message></span>
            </li>
            
            <xsl:if test="mods:language/mods:languageTerm">
            <li>
                <span class="label"><fmt:message bundle="${lctx}">common.language</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:language/mods:languageTerm" /></span>
            </li>
            </xsl:if>
                
            <xsl:if test="mods:name[@type='personal']/mods:role/mods:roleTerm = 'Author'">
            <h3><fmt:message bundle="${lctx}">common.author</fmt:message></h3>
            <xsl:for-each select="mods:name[@type='personal']"><li>
                <span class="value">
                    <xsl:if test="./mods:role/mods:roleTerm = 'Author'">
                        <div>
                            <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                            <xsl:value-of select="./mods:namePart[@type='given']" />
                        </div>
                    </xsl:if>
               </span>
            </li></xsl:for-each>
            </xsl:if>
            
            <xsl:if test="count(mods:originInfo[@transliteration='publisher'])=1">
                <h3><fmt:message bundle="${lctx}">Vydavatel</fmt:message></h3>
            </xsl:if>
            
            <xsl:if test="count(mods:originInfo[@transliteration='publisher'])>1">
                <h3><fmt:message bundle="${lctx}">Vydavatele</fmt:message></h3>
            </xsl:if>
            <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
            <li>                    
                <span class="label"><fmt:message bundle="${lctx}">Název vydavatele</fmt:message></span>
                <span class="value"><xsl:if test="./mods:publisher">
                     <xsl:value-of select="./mods:publisher" />
                </xsl:if></span>
            </li>
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Datum vydání</fmt:message></span>
                <span class="value"><xsl:if test="./mods:dateIssued">
                      <xsl:value-of select="./mods:dateIssued" />
                </xsl:if></span>
            </li>
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Místo vydání</fmt:message></span>
                <span class="value"><xsl:if test="./mods:place/mods:placeTerm">
                    <xsl:value-of select="./mods:place/mods:placeTerm" />
                    </xsl:if></span>
            </li>
            <h3></h3>
            </xsl:for-each>
            <xsl:if test="mods:originInfo[@transliteration='printer']">
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Název tiskaře</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" /></span>
            </li>
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Místo tisku</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" /></span>
            </li>
            </xsl:if>
            
            <xsl:if test="mods:physicalDescription/mods:extent">
                <h3><fmt:message bundle="${lctx}">Fyzický popis</fmt:message></h3>
                <xsl:choose>
                <xsl:when test="contains(mods:physicalDescription/mods:extent, ',')">
                    <li>
                        <span class="label"><fmt:message bundle="${lctx}">Rozměry</fmt:message></span>
                        <span class="value"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></span>
                    </li>
                    <li>
                        <span class="label"><fmt:message bundle="${lctx}">Rozsah</fmt:message></span>
                        <span class="value"><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></span>
                    </li>
                </xsl:when>
                <xsl:otherwise>
                    <li>
                        <span class="label"><fmt:message bundle="${lctx}">Rozsah</fmt:message></span>
                        <span class="value"><xsl:value-of select="mods:physicalDescription/mods:extent" /></span>
                    </li>
                </xsl:otherwise>
            </xsl:choose>
            </xsl:if>
            
            <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <h3><fmt:message bundle="${lctx}">Stav z hlediska ochrany fondů</fmt:message></h3>
            <li>
                <span class="value">
                        <fmt:message bundle="${lctx}">Aktuální stav</fmt:message>: 
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                </span>
            </li>
            </xsl:if>
            <xsl:if test="mods:location/mods:physicalLocation">
            <li>
                <span class="label"><fmt:message bundle="${lctx}">Místo uložení</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:location/mods:physicalLocation" /></span>
            </li>
            </xsl:if>
            <xsl:if test="mods:location/mods:shelfLocator">
            <li>
                <span class="label"><fmt:message bundle="${lctx}">common.signature</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:location/mods:shelfLocator" /></span>
            </li>
            </xsl:if>
            
            <xsl:if test="mods:originInfo/mods:frequency">
            <li>    
                <span class="label"><fmt:message bundle="${lctx}">Periodicita</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:originInfo/mods:frequency" /></span>
            </li>
            </xsl:if>
            <xsl:if test="mods:part/mods:detail[@type='regularsupplement']">
            <li>    
                <span class="label"><fmt:message bundle="${lctx}">Pravidelná příloha</fmt:message></span>
                <span class="value"><xsl:value-of select="mods:part/mods:detail[@type='regularsupplement']" /></span>
            </li>
            </xsl:if>
            
            <xsl:if test="mods:physicalDescription/mods:note">
            <h3><fmt:message bundle="${lctx}">Poznámky</fmt:message></h3>
            <li>
                <span class="value"><xsl:value-of select="mods:physicalDescription/mods:note" /></span>
            </li>
            </xsl:if>
                    
            </ul>
        </div></c:if>
        
    </xsl:template>
</xsl:stylesheet>
