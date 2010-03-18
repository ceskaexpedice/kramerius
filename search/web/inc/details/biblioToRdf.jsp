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
    <c:when test="${empty uuid || uuid==null || uuid==''}">
        <c:set var="pid" ><c:out value="${param.pid}" /></c:set>
    </c:when>
    <c:otherwise>
        <c:set var="pid" ><c:out value="${uuid}" /></c:set>
    </c:otherwise>
</c:choose>
<c:set var="urlPageStr" >
    <c:out value="${kconfig.fedoraHost}" />/get/<c:out value="${pid}" />/BIBLIO_MODS
</c:set>
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
        <jsp:useBean id="exceptions" type="java.lang.Exception" />
        <% System.out.println(exceptions); %>
    </c:when>
    <c:otherwise>
        <c:catch var="exceptions2"> 
            <% out.clear(); %>
            <x:transform doc="${xmlPage}"  xslt="${xsltPage}"  >
                <x:param name="pid" value="${pid}"/>
            </x:transform>
        </c:catch>
        <c:choose>
            <c:when test="${exceptions2 != null}" >
                <%--
                <c:out value="${exceptions}" /><br/>
                xsl --- <c:out value="${xsl}" /><br/>
                xslPage --- <c:out value="${xslPage}" /><br/>
                xsltPage --- <c:out value="${xsltPage}" />
                --%>
                <jsp:useBean id="exceptions2" type="java.lang.Exception" />
                <% System.out.println(exceptions2); %>
            </c:when>
            <c:otherwise></c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>