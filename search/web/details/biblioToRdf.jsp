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
            <c:out value="${fedoraHost}" />/get/<c:out value="${param.pid}" />/BIBLIO_MODS
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="urlPageStr" >
            <c:out value="${fedoraHost}" />/get/<c:out value="${uuid}" />/BIBLIO_MODS
        </c:set>
    </c:otherwise>
</c:choose>

<c:url var="urlPage" value="${urlPageStr}" />

<c:choose>
    <c:when test="${xsl==null || xsl==''}">
        <c:set var="xsl" value="xsl/${param.xsl}" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="xsl" value="${xsl}" scope="request" />
    </c:otherwise>
</c:choose>
<c:url var="xslPage" value="${xsl}" >
    <c:param name="language" value="${param.language}" />
    <c:param name="title" value="${param.title}" />
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${urlPage}" var="xmlPage" charEncoding="UTF-8"  />
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}" >
        <c:out value="${exceptions}" /><br/><br/>
    </c:when>
    <c:otherwise>
        <c:catch var="exceptions"> 
            <x:transform doc="${xmlPage}"  xslt="${xsltPage}"  />
        </c:catch>
        <c:choose>
            <c:when test="${exceptions != null}" >
                <c:out value="${exceptions}" /><br/>
                xsl --- <c:out value="${xsl}" /><br/>
                xslPage --- <c:out value="${xslPage}" /><br/>
                xsltPage --- <c:out value="${xsltPage}" />
            </c:when>
            <c:otherwise></c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>