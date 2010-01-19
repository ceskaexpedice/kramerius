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
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Typ volné části monografie</fmt:message>:</b><br/>
                <dd><xsl:choose>
                    <xsl:when test="mods:part/@type='Volume'">
                        <fmt:message>Volume</fmt:message></xsl:when>
                    <xsl:when test="mods:part/@type='chapter'">
                        <fmt:message>chapter</fmt:message></xsl:when>
                    <xsl:when test="mods:part/@type='section'">
                        <fmt:message>section</fmt:message></xsl:when>
                        <xsl:otherwise>
                        <fmt:message><xsl:value-of select="mods:part/@type" /></fmt:message>
                        </xsl:otherwise>
                </xsl:choose></dd>
            </span>
        </div>
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Identifikace části monografie</fmt:message>:</b><br/>
            </span>
        </div>
        <div><span valign="top"></span>
            <span>
                <b><fmt:message>Číslo části monografie</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:part/mods:detail/mods:number" /></dd>
            </span>
        </div>
        <div><span valign="top"></span>
            <span>
                <b><fmt:message>Název části monografie</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:part/mods:detail/mods:title" /></dd>
            </span>
        </div>
        <hr class="soft"></hr>
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Hlavní název</fmt:message>:</b><br/>
                <dd><a>
                        <xsl:attribute name="href">./item.jsp?pid=uuid:<xsl:value-of select="./mods:identifier[@type='urn']"/>&amp;model=info:fedora/model:monograph</xsl:attribute><xsl:value-of select="mods:titleInfo/mods:title" /></a></dd>
            </span>
            <c:if test="${display == 'none'}"><a onclick="$('#moreDetails').toggle();" href="#">more</a></c:if>
        </div>
        <div id="moreDetails">
            <xsl:attribute name="style">display:<c:out value="${param.display}" />;</xsl:attribute>
            
        <xsl:if test="mods:titleInfo[@type='alternative']/mods:title">
        <div><span valign="top"></span>
            <span>
                <b><fmt:message>Souběžný název</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:titleInfo[@type='alternative']/mods:title" /></dd>
            </span>
        </div>
        </xsl:if>
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Autor</fmt:message>:</b><br/>
                <xsl:for-each select="mods:name[@type='personal']">
                    <xsl:if test="./mods:role/mods:roleTerm = 'Author'">
                        <dd>
                            <b>Příjmení: </b>&#160;<xsl:value-of select="./mods:namePart[@type='family']" />&#160;&#160;
                            <b>Jméno: </b>&#160;<xsl:value-of select="./mods:namePart[@type='given']" />
                        </dd>
                    </xsl:if>
                </xsl:for-each>
            </span>
        </div>
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Druh dokumentu</fmt:message>:</b><br/>
                <dd>
                    <fmt:message>info:fedora/model:monograph</fmt:message>
                </dd>
            </span>
        </div>
        <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
            <div><span valign="top">*</span>
                <span>
                    <xsl:if test="./mods:publisher">
                    <b><fmt:message>Název vydavatele</fmt:message>:</b><br/>
                        <dd>
                            <xsl:value-of select="./mods:publisher" />
                        </dd><br/>
                    </xsl:if>
                    <xsl:if test="./mods:dateIssued">
                    <b><fmt:message>Datum vydání</fmt:message>:</b><br/>
                        <dd>
                            <xsl:value-of select="./mods:dateIssued" />
                        </dd><br/>
                    </xsl:if>
                    <xsl:if test="./mods:place/mods:placeTerm">
                    <b><fmt:message>Místo vydání</fmt:message>:</b><br/>
                        <dd>
                            <xsl:value-of select="./mods:place/mods:placeTerm" />
                        </dd><br/>
                    </xsl:if>
                </span>
            </div>
        </xsl:for-each>
        <xsl:if test="mods:originInfo[@transliteration='printer']">
            <div><span valign="top">*</span>
                <span>
                    <b><fmt:message>Název tiskaře</fmt:message>:</b><br/> 
                    <dd>
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" />
                    </dd>
                    <br/> 
                    <b><fmt:message>Místo tisku</fmt:message>:</b><br/>
                    <dd> 
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:extent">
            <div><span valign="top">*</span>
                <span>
                    <b><fmt:message>Fyzický popis</fmt:message>:</b><br/>
                    
                    <b><fmt:message>Rozměry</fmt:message>:</b><br/> 
                    <dd><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></dd>
                    <br/>
                    <b><fmt:message>Rozsah</fmt:message>:</b><br/> 
                    <dd><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></dd>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <div><span valign="top">*</span>
                <span>
                    <b><fmt:message>Stav z hlediska ochrany fondů</fmt:message>:</b><br/>
                    <dd>
                        <b><fmt:message>Aktuální stav</fmt:message>:</b><br/> 
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        
        <xsl:if test="mods:location/mods:physicalLocation">
            <div><span valign="top">*</span>
                <span>
                    <b><fmt:message>Místo uložení</fmt:message>:</b><br/>
                    <dd>
                        <xsl:value-of select="mods:location/mods:physicalLocation" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:location/mods:shelfLocator">
            <div><span valign="top">*</span>
                <span>
                    <b><fmt:message>Signatura</fmt:message>:</b><br/>
                    <dd>
                        <xsl:value-of select="mods:location/mods:shelfLocator" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        </div>
        <div>
        <span class="textpole"><a href="javascript:reIndex('<x:out select="./str[@name='PID']"/>', '<x:out select="./str[@name='PID']"/>');">re-index</a></span>
        </div>
        <div id="reindex_<x:out select="./str[@name='PID']"/>"> </div>
    </xsl:template>
</xsl:stylesheet>
