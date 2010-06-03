<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<!-- pouzite filtry -->
<c:if test="${!empty param.q}" >
<div class="usedFilter">
    :: <a title="" class="mainNav"
     href="javascript:removeQuery();"><c:out value="${param.q}" /><img src="img/x.png"  border="0" 
     title="<fmt:message bundle="${lctx}" key="remove_criteria"/><fmt:message bundle="${lctx}" key="query" />: <c:out value="${param.q}" />" />
</a></div>
</c:if>
<%-- datum --%>
<c:if test="${param.f1 != null}">
    <fmt:message bundle="${lctx}" key="Datum" />: <c:out value="${param.f1}" /> - <c:out value="${param.f2}" />
</c:if>

<%-- filter queries --%>
<c:forEach var="fqs" items="${paramValues.fq}">
    
    <c:set var="js"><c:out value="${fn:replace(fqs, '\"', '')}" /></c:set>
    <c:set var="facetName"><c:out value="${fn:substringBefore(fqs,':')}" /></c:set>
    <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
    <c:set var="facetValue"><c:out value="${fn:substringAfter(fqs,':')}" escapeXml="false" /></c:set>
    <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
    <c:if test="${facetName == 'fedora.model'}">
        <c:set var="facetName"><fmt:message bundle="${lctx}" ><c:out value="${facetName}" /></fmt:message></c:set>
        <c:set var="facetValue"><fmt:message bundle="${lctx}" ><c:out value="${facetValue}" /></fmt:message></c:set>
    </c:if>
    <div class="usedFilter">:: <a title="" class="mainNav" href="javascript:removeNavigation2('<c:out value="${facetName}" />', '<c:out value="${facetValue}" />');">
    <c:out value="${facetName}" />: <c:out value="${facetValue}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: <c:out value="${facetName}"/>"/>
        </a></div>
</c:forEach>

<%-- advanced params --%>
<c:if test="${!empty param.issn}">
    <div class="usedFilter">:: <a title="" class="mainNav" href="javascript:removeNavigation2('issn', '<c:out value="${param.issn}" />');">
    <fmt:message bundle="${lctx}" key="issn" />: <c:out value="${param.issn}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: issn"/></a></div>
</c:if>
<c:if test="${!empty param.title}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('title', '<c:out value="${param.title}" />');">
    <fmt:message bundle="${lctx}">Hlavní název</fmt:message>: <c:out value="${param.title}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: title"/></a></div>
</c:if>
<c:if test="${!empty param.author}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('author', '<c:out value="${param.author}" />');">
    <fmt:message bundle="${lctx}" key="author" /> <c:out value="${param.author}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: author"/></a></div>
</c:if>
<c:if test="${!empty param.rok}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('rok', '<c:out value="${param.rok}" />');">
    <fmt:message bundle="${lctx}" key="rok" /> <c:out value="${param.rok}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: rok"/></a></div>
</c:if>
<c:if test="${!empty param.udc}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('udc', '<c:out value="${param.udc}" />');">
    <fmt:message bundle="${lctx}" key="MDT" /> <c:out value="${param.udc}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: udc"/></a></div>
</c:if>
<c:if test="${!empty param.ddc}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('ddc', '<c:out value="${param.ddc}" />');">
    <fmt:message bundle="${lctx}" key="DDT" /> <c:out value="${param.ddc}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: ddc"/></a></div>
</c:if>
<c:if test="${!empty param.onlyPublic}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('onlyPublic', '<c:out value="${param.onlyPublic}" />');">
    <fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" /> <c:out value="${param.onlyPublic}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: onlyPublic"/></a></div>
</c:if>
<%--
<br/>
<x:if select="$doc/response/lst/lst/arr[@name='fq'] or //str[@name='fq']">
    <x:forEach select="$doc/response/lst/lst/arr[@name='fq']/str">
        <c:set var="facetName"><x:out select="substring-before(.,':')" /></c:set>
        <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
        <c:set var="facetValue"><x:out select="substring-after(.,':')" escapeXml="false" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
        <c:if test="${facetName == 'fedora.model'}">
            <c:set var="facetName"><fmt:message bundle="${lctx}" ><c:out value="${facetName}" /></fmt:message></c:set>
            <c:set var="facetValue"><fmt:message bundle="${lctx}" ><c:out value="${facetValue}" /></fmt:message></c:set>
        </c:if>
        - <a title="" class="mainNav"
             href="javascript:removeNavigation('<x:out select="." />');">
            <c:out value="${facetValue}"/>&#160;<img src="img/x.png"  border="0" 
                                                     title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: <c:out value="${facetName}"/>"/>
        </a>
    </x:forEach>
    <x:forEach select="$doc/response/lst/lst/str[@name='fq']">
        <c:set var="facetName"><x:out select="substring-before(.,':')" /></c:set>
        <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
        <c:set var="facetValue"><x:out select="substring-after(.,':')" escapeXml="false" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
        <c:if test="${facetName == 'fedora.model'}">
            <c:set var="facetName"><fmt:message bundle="${lctx}" ><c:out value="${facetName}" /></fmt:message></c:set>
            <c:set var="facetValue"><fmt:message bundle="${lctx}" ><c:out value="${facetValue}" /></fmt:message></c:set>
        </c:if>
        - <a title="" class="mainNav"
             href="javascript:removeNavigation('<x:out select="." />');">
            <c:out value="${facetValue}"/>&#160;<img src="img/x.png"  border="0" 
                                                     title="<fmt:message bundle="${lctx}" key="remove_criteria"/>: <c:out value="${facetName}"/>"/>
        </a>
    </x:forEach>
    
</x:if>
--%>
<!-- konec pouzite filtry -->

