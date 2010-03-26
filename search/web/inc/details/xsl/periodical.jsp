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
   xmlns:mods="http://www.loc.gov/mods/v3"  exclude-result-prefixes="mods" >
    <xsl:output method="html" indent="yes" encoding="UTF-8"  />
    <!-- TODO customize transformation rules 
    syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info" >
        <div><c:if test="${display == 'none'}">
                    <xsl:attribute name="onclick" >
                        javascript:showMainContent('<xsl:value-of select="./mods:identifier[@type='urn']"/>', 'periodical')
                    </xsl:attribute></c:if>
       
        <div><span>*</span>
            <span>
                <b>ISSN:</b><br/>
                <div><xsl:value-of select="./mods:identifier[@type='issn']" /></div>
            </span>
        </div>
        
        <div><span>*</span>
            <span>
                <b><fmt:message>Hlavní název</fmt:message>:</b><br/>
                <div><span id="periodicaltitle"><xsl:value-of select="mods:titleInfo/mods:title" /></span></div>
            </span>
        </div>
        <xsl:if test="mods:titleInfo/mods:subTitle">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Podnázev</fmt:message>:</b><br/> 
                    <div>
                        <xsl:value-of select="mods:titleInfo/mods:subTitle" />
                    </div>
                </span>
            </div>
        </xsl:if>
         </div>
        <div id="moreDetails">
            <xsl:attribute name="style">display:<c:out value="${param.display}" />;</xsl:attribute>
            
        <div><span>*</span>
            <span>
                <b><fmt:message>Druh dokumentu</fmt:message>:</b><br/>
                <div>
                    <fmt:message>info:fedora/model:periodical</fmt:message>
                </div>
            </span>
        </div>
        <xsl:if test="mods:originInfo[@transliteration='publisher']">
            <div><table>
                    <tr><td><b><fmt:message>Název vydavatele</fmt:message>:</b></td>
                    <td><b><fmt:message>Datum vydání</fmt:message>:</b></td>
                    <td><b><fmt:message>Místo vydání</fmt:message>:</b></td></tr>
            
        <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
                <tr>
                    <xsl:if test="./mods:publisher">
                        <td>
                            <xsl:value-of select="./mods:publisher" />
                        </td>
                    </xsl:if>
                    <xsl:if test="./mods:dateIssued">
                        <td>
                            <xsl:value-of select="./mods:dateIssued" />
                        </td>
                    </xsl:if>
                    <xsl:if test="./mods:place/mods:placeTerm/text()">
                        <td>
                            <xsl:value-of select="./mods:place/mods:placeTerm" />
                        </td>
                    </xsl:if>
                </tr>
            
        </xsl:for-each>
            </table>
        </div>
        </xsl:if>
        <div><span>*</span>
            <span>
                <b><fmt:message>Jazyk</fmt:message>:</b><br/>
                <div>
                    <xsl:value-of select="mods:language/mods:languageTerm" />
                </div>
            </span>
        </div>
        <xsl:if test="mods:originInfo[@transliteration='printer']">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Název tiskaře</fmt:message>:</b><br/> 
                    <div>
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" />
                    </div>
                    <br/> 
                    <b><fmt:message>Místo tisku</fmt:message>:</b><br/>
                    <div> 
                        <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:extent/text()">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Fyzický popis</fmt:message>:</b><br/>
                    <b><fmt:message>Rozměry</fmt:message>:</b><br/> 
                    <div><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></div>
                    <br/>
                    <b><fmt:message>Rozsah</fmt:message>:</b><br/> 
                    <div><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:note/text()">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Poznámky</fmt:message>:</b><br/>
                    <div><xsl:value-of select="mods:physicalDescription/mods:note" /></div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Stav z hlediska ochrany fondů</fmt:message>:</b><br/>
                    <div>
                        <b><fmt:message>Aktuální stav</fmt:message>:</b><br/> 
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                    </div>
                </span>
            </div>
        </xsl:if>
        
        <xsl:if test="mods:location/mods:physicalLocation">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Místo uložení</fmt:message>:</b><br/>
                    <div>
                        <xsl:value-of select="mods:location/mods:physicalLocation" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:location/mods:shelfLocator">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Signatura</fmt:message>:</b><br/>
                    <div>
                        <xsl:value-of select="mods:location/mods:shelfLocator" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:originInfo/mods:frequency">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Periodicita</fmt:message>:</b><br/>
                    <div>
                        <xsl:value-of select="mods:originInfo/mods:frequency" />
                    </div>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:part/mods:detail[@type='regularsupplement']">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Pravidelná příloha</fmt:message>:</b><br/>
                    <div>
                        <xsl:value-of select="mods:part/mods:detail[@type='regularsupplement']" />
                    </div>
                </span>
            </div>
        </xsl:if>
        </div>
    </xsl:template>
</xsl:stylesheet>
