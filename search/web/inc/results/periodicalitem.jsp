<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div>
    <span>
        <c:set var="itemPid" >
            <x:out select="./str[@name='PID']"/>
        </c:set>
        <%--
        <x:choose>
            
            <x:when select="./str[@name='title_to_show2']">
                <c:set var="itemUrl" >
                    ./item.jsp?pid=<x:out select="./str[@name='PID']"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&parentPid=<x:out select="substring-after(substring-after(substring-after(./str[@name='title_to_show'], '###'), '###'), 'info:fedora/')"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&model=info:fedora/model:page&page=<x:out select="./arr[@name='dc.title']"/>
                </c:set>
                <a href="<c:out value="${itemUrl}" escapeXml="false" />" >
                <b><x:out select="substring-before(./str[@name='title_to_show'], '###')"/></b></a> <x:out select="substring-after(./str[@name='title_to_show'], '###')"/>
                
                (<fmt:message>info:fedora/model:periodicalitem</fmt:message> <fmt:message>info:fedora/model:periodical</fmt:message>)
                <br/>
                <fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>: <x:out select="./arr[@name='dc.title']"/>
            </x:when>
            <x:otherwise>
                <a href="<c:out value="${fedoraHost}" />/get/<x:out select="./str[@name='PID']"/>">
                    <b><x:out select="./arr[@name='dc.title']"/></b>
                </a> (<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>)
            </x:otherwise>
        </x:choose>
        --%>
        <a href="<c:out value="${fedoraHost}" />/get/<x:out select="./str[@name='PID']"/>">
            <b><x:out select="./arr[@name='dc.title']"/></b>
        </a> (<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message> 
        <fmt:message>info:fedora/model:periodical</fmt:message>)
        <br/>
        
        <c:set var="urlStr" >
            <c:out value="${fedoraHost}" />/get/<c:out value="${itemPid}" />/BIBLIO_MODS
        </c:set>
        <c:url var="urlGet" value="${urlStr}" >
        </c:url>
        <c:catch var="exceptions"> 
            <c:import url="${urlGet}" var="xml2" charEncoding="UTF-8"  />
            <c:set var="xslt" >
                <?xml version="1.0"?>
                <xsl:stylesheet version="1.0"
                                xmlns:xsl= "http://www.w3.org/1999/XSL/Transform"
                                xmlns:mods="http://www.loc.gov/mods/v3">
                    <xsl:output omit-xml-declaration="yes" />
                    <xsl:template match="/">
                        <xsl:if test="//mods:modsCollection/mods:mods/mods:titleInfo/mods:subTitle != ''">
                        <xsl:value-of select="//mods:modsCollection/mods:mods/mods:titleInfo/mods:subTitle" />
                        <br/></xsl:if>
                        <fmt:message>info:fedora/model:periodicalitem</fmt:message>: <xsl:value-of select="//mods:modsCollection/mods:mods/mods:part[@type = 'PeriodicalIssue']/mods:date" />
                    </xsl:template>
                </xsl:stylesheet>
                
            </c:set>
        </c:catch>
        <c:choose>
            <c:when test="${exceptions != null}" >
                <c:out value="${exceptions}" /><br/><br/>
            </c:when>
            <c:otherwise>
                <x:transform doc="${xml2}"  xslt="${xslt}"  />
                <%--x:out select="$itemDoc/mods:modsCollection/mods:mods/mods:titleInfo/mods:subTitle"/--%>
            </c:otherwise>
        </c:choose>
        
    </span>
</div>
