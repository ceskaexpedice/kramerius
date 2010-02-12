<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>
<c:set var="uuid" >
    <x:out select="./str[@name='PID']"/>
</c:set>
<c:set var="uuidSimple" >
    <x:out select="substring-after(./str[@name='PID'], 'uuid:')"/>
</c:set>
<div>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <span>
                <a href="./item.jsp?pid=<x:out select="./str[@name='PID']"/>&model=info:fedora/model:periodical">
                <b><x:out select="./str[@name='dc.title']"/></b>
                </a> 
    </span>
    <span class="textpole">(<fmt:message>info:fedora/model:periodical</fmt:message>)</span>
    <span id="pages_<c:out value="${uuidSimple}"/>" class="pages"><x:out select="./int[@name='pages_count']"/> </span>
    <br/>
    <x:out select="./str[@name='datum']"/>
</div>