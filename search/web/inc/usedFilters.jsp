<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<!-- pouzite filtry -->
<c:if test="${!empty param.q || param.f1 != null && param.f1 != '' || !empty paramValues.fq ||
              !empty param.issn || !empty param.title || !empty param.author || !empty param.rok || !empty param.keywords ||
              !empty param.udc ||!empty param.ddc || !empty param.onlyPublic || param.suggest=='true' }" >
<table class="main usedFilters"><tr valign='top'><td>
<c:if test="${!empty param.q}" >
<div class="usedFilter">
    :: <a class="mainNav"
     href="javascript:removeQuery();">
     <c:out value="${param.q}" />
     <img src="img/x.png"  border="0" 
     title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/><fmt:message bundle="${lctx}" key="filter.query" />: <c:out value="${param.q}" />" />
</a></div>
</c:if>
<%-- datum --%>
<c:if test="${param.f1 != null && param.f1 != ''}">
    <div class="usedFilter">::
    <a title="" class="mainNav" href="javascript:removeDateAxisFilter();">
    <fmt:message bundle="${lctx}" key="common.date" />: <c:out value="${param.f1}" /> - <c:out value="${param.f2}" />&#160;<img src="img/x.png"  border="0"
        title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/> : <fmt:message bundle="${lctx}" key="common.date" />"/></a></div>


</c:if>

<%-- filter queries --%>
<c:forEach varStatus="status" var="fqs" items="${paramValues.fq}">
   
        <c:set var="js"><c:out value="${fn:replace(fqs, '\"', '')}" /></c:set>
        <c:set var="facetName"><c:out value="${fn:substringBefore(fqs,':')}" /></c:set>
        <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:substringAfter(fqs,':')}" escapeXml="false" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
        <c:set var="facetValueDisp"><c:out value="${facetValue}" /></c:set>
        <c:if test="${facetName == 'fedora.model' || facetName == 'document_type'}">
            <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >fedora.model.<c:out value="${facetValueDisp}" /></fmt:message></c:set>
        </c:if>
        <c:if test="${facetName == 'dostupnost'}">
            <c:set var="facetValueDisp"><fmt:message bundle="${lctx}" >dostupnost.<c:out value="${facetValueDisp}" /></fmt:message></c:set>
        </c:if>
        <div class="usedFilter">:: <a  class="mainNav" href="javascript:removeFacet(<c:out value="${status.count}" />);">
        <fmt:message bundle="${lctx}" ><c:out value="${facetName}" /></fmt:message>: <c:out value="${facetValueDisp}"/>&#160;<img src="img/x.png"  border="0" 
        title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <c:out value="${facetName}"/>"/>
            </a></div><input type="hidden" name="fq" id="fq<c:out value="${status.count}" />" value="<c:out value="${facetName}" />:<c:out value="${facetValue}" />" />
    
</c:forEach>
            
<%-- suggest params --%>
<c:if test="${param.suggest=='true'}">


        <c:set var="facetName"><c:out value="${fn:substringBefore(param.suggest_q,':')}" /></c:set>
        <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:substringAfter(param.suggest_q,':')}" escapeXml="false" /></c:set>
        <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
        <c:set var="facetValueDisp"><c:out value="${facetValue}" /></c:set>


        <div class="usedFilter">:: <a  class="mainNav" href="javascript:removeSuggest();">
        <fmt:message bundle="${lctx}" key="Procházet" />: <fmt:message bundle="${lctx}" >suggest.<c:out value="${facetName}"/></fmt:message> - <c:out value="${facetValueDisp}"/>&#160;<img src="img/x.png"  border="0"
        title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <c:out value="${param.suggest_q}"/>"
        alt="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <c:out value="${param.suggest_q}"/>" />
            </a></div><input type="hidden" name="suggest_q" id="suggest_q" value="<c:out value="${param.suggest_q}" />" />
            <input type="hidden" name="suggest" id="suggest" value="true" />
</c:if>

<%-- advanced params --%>
<c:if test="${!empty param.issn}">
    <div class="usedFilter">:: <a title="" class="mainNav" href="javascript:removeNavigation2('issn', '<c:out value="${param.issn}" />');">
    <fmt:message bundle="${lctx}" key="issn" />: <c:out value="${param.issn}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: issn"/></a></div>
</c:if>
<c:if test="${!empty param.title}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('title', '<c:out value="${param.title}" />');">
    <fmt:message bundle="${lctx}">filter.maintitle</fmt:message>: <c:out value="${param.title}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: title"/></a></div>
</c:if>
<c:if test="${!empty param.author}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('author', '<c:out value="${param.author}" />');">
    <fmt:message bundle="${lctx}" key="author" /> &#160;<c:out value="${param.author}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: author"/></a></div>
</c:if>
<c:if test="${!empty param.rok}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('rok', '<c:out value="${param.rok}" />');">
    <fmt:message bundle="${lctx}" key="rok" />: &#160;<c:out value="${param.rok}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: rok"/></a></div>
</c:if>
<c:if test="${!empty param.keywords}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('keywords', '<c:out value="${param.keywords}" />');">
    <fmt:message bundle="${lctx}" key="Keywords" />: &#160;<c:out value="${param.keywords}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: keywords"/></a></div>
</c:if>
<c:if test="${!empty param.udc}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('udc', '<c:out value="${param.udc}" />');">
    MDT: &#160;<c:out value="${param.udc}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: udc"/></a></div>
</c:if>
<c:if test="${!empty param.ddc}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('ddc', '<c:out value="${param.ddc}" />');">
    DDT: &#160;<c:out value="${param.ddc}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: ddc"/></a></div>
</c:if>
<c:if test="${!empty param.onlyPublic}">
    <div class="usedFilter">:: 
    <a title="" class="mainNav" href="javascript:removeNavigation2('onlyPublic', '<c:out value="${param.onlyPublic}" />');">
    <fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" />:&#160; <c:out value="${param.onlyPublic}"/>&#160;<img src="img/x.png"  border="0" 
    title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <fmt:message bundle="${lctx}" key="Pouze veřejné dokumenty" />" /></a></div>
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
                                                     title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <c:out value="${facetName}"/>"/>
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
                                                     title="<fmt:message bundle="${lctx}" key="filter.remove_criteria"/>: <c:out value="${facetName}"/>"/>
        </a>
    </x:forEach>
    
</x:if>
--%>
</td></tr></table>
</c:if>
<!-- konec pouzite filtry -->

