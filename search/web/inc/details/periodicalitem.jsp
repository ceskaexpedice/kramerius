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
        <div><h3><span id="periodicaltitle"><c:out value="${param.title}" /></span></h3></div>
        <hr class="soft" />
        <div><span valign="top">*</span> 
            <span>
                <b><fmt:message>Datum vydání výtisku</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:part/mods:date" /></dd>
            </span>
        </div>
        <div><span valign="top">*</span> 
            <span>
                <b><fmt:message>Číslo výtisku</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:part/mods:detail[@type = 'issue']/mods:number" /></dd>
            </span>
        </div>
        <div><span valign="top">*</span>
            <span>
                <b><fmt:message>Hlavní název</fmt:message>:</b><br/>
                <dd><xsl:value-of select="mods:titleInfo/mods:title" /></dd>
            </span>
            
        </div>
        <xsl:if test="mods:physicalDescription/mods:note">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Poznámky</fmt:message>:</b><br/>
                    <dd>
                        <xsl:value-of select="mods:physicalDescription/mods:note" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        <xsl:if test="mods:part/mods:text">
            <div><span>*</span>
                <span>
                    <b><fmt:message>Vady</fmt:message>:</b><br/>
                    <dd>
                        <xsl:value-of select="mods:part/mods:text" />
                    </dd>
                </span>
            </div>
        </xsl:if>
        <hr class="soft"></hr>
    </xsl:template>
</xsl:stylesheet>
