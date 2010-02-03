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
<%//c:url var="url" value="http://localhost:8983/solr/select/select" 
//http://194.108.215.227:8080/solr/select?indent=on&version=2.2&q=fedora.model%3A%22info%3Afedora%2Fmodel%3Apage%22&start=0&rows=10&fl=*%2Cscore&qt=standard&wt=xslt&explainOther=&hl.fl=&facet=true&facet.field=fedora.model&tr=example.xsl
%> 
<c:set var="filters" scope="request" ></c:set>
<c:set var="pageType" value="search" />
<jsp:useBean id="pageType" type="java.lang.String" />


<c:url var="url" value="${fedoraSolr}" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
            <c:if test="${empty param.fq}">
                <c:param name="rows" value="0" />
            </c:if>
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
            <c:set var="rows"  value="55" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:param name="facet" value="false" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
        <c:set var="filters" scope="request"><c:out value="${filters}" />&fq=<c:out value="${fqs}" /></c:set>
    </c:forEach>
    <c:if test="${param.f1 != null}">
        <c:param name="fq" value="rok:[${param.f1} TO ${param.f2}]" />
    </c:if>
    <c:param name="start" value="${param.offset}" />
    
    <c:param name="sort" value="title asc" />
    
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:import url="empty.xml" var="xml" />
        <c:out value="${exceptions}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <x:parse var="doc" xml="${xml}"  />
        <c:set var="numDocs" scope="request" >
            <x:out select="$doc/response/result/@numFound" />
        </c:set>
        <div id="s_<c:out value="${param.d}" />">
            <x:forEach select="$doc/response/result/doc">
                <div id="<x:out select="./str[@name='PID']"/>" class="inTree">
                    <a href="./item.jsp?pid=<x:out select="./str[@name='PID']"/>&&model=<x:out select="./str[@name='fedora.model']"/>">
                    <x:out select="./str[@name='dc.title']"/></a> 
                </div>
            </x:forEach>
        </div>
        <div id="paginationInTree" align="right">
            <%@ include file="paginationPageNum.jsp" %>
        </div> 
    </c:otherwise>
</c:choose>

