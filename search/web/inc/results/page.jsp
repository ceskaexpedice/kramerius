<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<div>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <span>
                <c:set var="itemUrl" >
                    ./item.jsp?pid=<x:out select="./str[@name='PID']"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&parentPid=<x:out select="substring-after(./str[@name='parent_pid'], 'info:fedora/')"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&parentModel=<x:out select="substring-after(./str[@name='parent_model'], 'info:fedora/')"/>
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&model=info:fedora/model:page&page=<x:out select="./arr[@name='dc.title']"/>
                </c:set>
            <x:choose>
            <x:when select="./str[@name='title_to_show']/text()">
                <a href="<c:out value="${itemUrl}" escapeXml="false" />" >
                    <b><x:out select="./str[@name='title_to_show']"/></b>
                </a>
                (<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message> 
                <fmt:message><x:out select="./str[@name='title_to_show']"/></fmt:message>)
                <br/>
                <fmt:message>info:fedora/model:<x:out select="./str[@name='fedora.model']"/></fmt:message>: <x:out select="./arr[@name='dc.title']"/>
            </x:when>
            <x:when select="./str[@name='root_title']/text()">
                <a href="<c:out value="${itemUrl}" escapeXml="false" />" >
                    <b><x:out select="./str[@name='root_title']"/></b>
                </a>
                (<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message> 
                <fmt:message>info:fedora/model:<x:out select="./str[@name='root_model']"/></fmt:message>)
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
    <span class="textpole"><a target="_blank" href="http://194.108.215.227:8080/fedoragsearch/rest?operation=updateIndex&action=fromPid&value=<x:out select="./str[@name='PID']"/>">re-index</a></span>
</div>
