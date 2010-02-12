<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false"%>
<%--
    Used to show short details in rdf lists
    Get the BIBLIO_MODS info from fedora for param.pid
    and output transformed by param.xsl
--%>
<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />

<c:choose>
    <c:when test="${uuid==null || uuid==''}">
        <c:set var="urlPageStr" >
            <c:out value="${kconfig.fedoraHost}" />/get/<c:out value="${param.pid}" />/RELS-EXT
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="urlPageStr" >
            <c:out value="${kconfig.fedoraHost}" />/get/<c:out value="${uuid}" />/RELS-EXT
        </c:set>
    </c:otherwise>
</c:choose>

<c:url var="urlPage" value="${urlPageStr}" />
<c:catch var="exceptions"> 
    <c:import url="${urlPage}" var="xmlPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}" >
        <c:out value="${exceptions}" /><br/><br/>
    </c:when>
    <c:otherwise>
        <c:catch var="exceptions"> 
            <x:parse var="doc" xml="${xmlPage}"  systemId="${urlPageStr}" />
        </c:catch>
        <c:choose>
            <c:when test="${exceptions != null}" >
                <c:out value="${exceptions}" /><br/>
                
            </c:when>
            <c:otherwise>
                <c:out value="${xmlPage}"/>
                <x:forEach varStatus="status" select="$doc//*[name()='rdf:Description']/*" >
                    
                    <x:out select="name()"/> --- <x:out select="./@*"/><br/>
                </x:forEach>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>