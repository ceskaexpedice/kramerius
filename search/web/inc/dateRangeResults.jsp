<%@ page contentType="application/x-javascript" pageEncoding="UTF-8" %>
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
<c:set var="fedoraSolr" value="http://194.108.215.227:8080/solr/select/select" />
<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<%--
http://194.108.215.227:8080/solr/select?
q=datum_begin%3A[1+TO+3010]
rows=0
facet=true
facet.field=datum
facet.mincount=1
f.datum.facet.sort=false
fq=root_model:periodical
fq=fedora.model:%22info:fedora/model:page%22
--%>

<c:url var="url" value="${fedoraSolr}" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
        </c:when>
        <c:when test="${param.q != null}" >
            <c:param name="q" value="${param.q}" />
        </c:when>
    </c:choose>
    <c:param name="rows" value="1000" />
    <c:param name="facet" value="false" />
    <c:param name="fl" value="datum,datum_begin,datum_end,dc.title,PID" />
    <c:param name="fq" value="datum_begin:[1 TO 3010]" />
    <c:param name="fq" value="level:0" />
    <c:param name="fq2" value="fedora.model:\"info:fedora/model:periodical\"" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
    </c:forEach>
    <c:if test="${param.f1 != null}">
        <c:param name="fq" value="rok:[${param.f1} TO ${param.f2}]" />
    </c:if>
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        error: <c:out value="${exceptions}" />
    </c:when>
    <c:otherwise>
        <x:parse var="doc" xml="${xml}"  />
        <c:set var="numDocs" scope="request" >
            <x:out select="$doc/response/result/@numFound" />
        </c:set>
<%out.clear();%>({
"items": [
<x:forEach varStatus="status" select="$doc/response/result/doc">
{
    "datum": "<x:out select="./str[@name='datum']"/>",
    "beginDate": <x:out select="./int[@name='datum_begin']"/>,
    "endDate": <x:out select="./int[@name='datum_end']"/>,
    "count": "<x:out select="./str[@name='dc.title']"/>"
}<c:if test="${status.count < numDocs}" >,</c:if>
</x:forEach>
]
})
    </c:otherwise>
</c:choose>
