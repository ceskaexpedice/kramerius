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

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<%@ include file="initVars.jsp" %>
<c:set var="filters" scope="request" ></c:set>
<c:set var="pageType" value="search" />
<jsp:useBean id="pageType" type="java.lang.String" />
<c:url var="url" value="${kconfig.solrHost}/select/" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
        </c:when>
        <c:when test="${param.q != null}" >
            <c:if test="${fn:containsIgnoreCase(param.q, '*')}" >
                <c:param name="qt" value="czparser" />
            </c:if>
            <c:param name="q" value="${param.q}" />
        </c:when>
        
    </c:choose>
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows"  value="10" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
        <c:set var="filters" scope="request"><c:out value="${filters}" />&fq=<c:out value="${fqs}" /></c:set>
    </c:forEach>
    <c:if test="${param.pid != null && param.pid != 'undefined' && param.pid != ''}" >
        <c:param name="fq" >parent_pid:"<c:out value="${param.pid}" />"</c:param>
    </c:if>
    <c:if test="${param.f1 != null}">
        <c:param name="fq" value="rok:[${param.f1} TO ${param.f2}]" />
    </c:if>
    <c:param name="start" value="${param.offset}" />
    
    <c:choose>
        <c:when test="${param.sort != null}" >
            <c:param name="sort" value="${param.sort}" />
        </c:when>
        <c:when test="${sort != null}" >
            <c:param name="sort" value="${sort}" />
        </c:when>
        <c:when test="${fieldedSearch}">
            <c:param name="sort" value="level asc, title asc, score desc" />
        </c:when>
        <c:when test="${empty param.q}" >
            <c:param name="sort" value="level asc, title asc, score desc" />
        </c:when>
        <c:otherwise>
            <c:param name="sort" value="level asc, score desc" />
        </c:otherwise>
    </c:choose>
    
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <x:parse var="doc" xml="${xml}"  />
        <%@ include file="proccessFacets.jsp" %>
        <c:set var="numDocs" scope="request" >
            <x:out select="$doc/response/result/@numFound" />
        </c:set>
        <div id="s_<c:out value="${param.d}" />">
            <%@ include file="resultsMain.jsp" %>
        </div>
        <div id="paginationInTree" align="right">
            <%@ include file="paginationPageNum.jsp" %>
        </div> 
        <div id="sub_<c:out value="${param.d}" />"></div>
    </c:otherwise>
</c:choose>
 <c:if test="${param.debug}" >
    <c:out value="${url}" /><br/>
    <c:out value="${param.parentPid}" />
</c:if>

