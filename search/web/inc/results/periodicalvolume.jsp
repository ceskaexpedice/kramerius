<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <span>
            <x:choose>
            <x:when select="./str[@name='title_to_show']">
                <c:set var="itemUrl" >
                    ./item.jsp?pid=<x:out select="./str[@name='PID']"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&parentPid=<x:out select="substring-after(substring-after(substring-after(./str[@name='title_to_show'], '###'), '###'), 'info:fedora/')"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&model=info:fedora/model:periodicalvolume&page=<x:out select="./arr[@name='dc.title']"/>
                </c:set>
                <a href="<c:out value="${itemUrl}" escapeXml="false" />" >
                    <b><x:out select="substring-before(./str[@name='title_to_show'], '###')"/></b>a
                </a>
                
                (<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>)
                <br/>
                <fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>: <x:out select="./arr[@name='dc.title']"/>
            </x:when>
            <x:otherwise>
                <a href="<c:out value="${fedoraHost}" />/get/<x:out select="./str[@name='PID']"/>">
                <b><x:out select="./arr[@name='dc.title']"/></b>
                </a> 
            </x:otherwise>
            </x:choose>
    </span>
</div>
