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
                    <c:out value="${itemUrl}" escapeXml="false" />&parentPid=<x:out select="./str[@name='parent_pid']" />
                </c:set>
                <c:set var="itemUrl" >
                    <c:out value="${itemUrl}" escapeXml="false" />&model=info:fedora/model:periodicalvolume&page=<x:out select="./str[@name='dc.title']"/>
                </c:set>
                <a href="<c:out value="${itemUrl}" escapeXml="false" />" >
                    <b><x:out select="./str[@name='root_title']"/></b>
                </a>&nbsp;<x:out select="./str[@name='dc.title']"/>&nbsp;
                (<fmt:message bundle="${lctx}"><x:out select="./str[@name='fedora.model']"/></fmt:message>)
                <br/>
                <fmt:message bundle="${lctx}">Datum vydání ročníku</fmt:message>: <x:out select="./int[@name='rok']"/><br/>
                <fmt:message bundle="${lctx}">Číslo ročníku</fmt:message>: <x:out select="./str[@name='dc.title']"/>
    </span>
</div>
