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
    <c:param name="q" value="datum_begin:[1 TO 3010]" />
    <c:param name="rows" value="0" />
    <c:param name="facet" value="true" />
    <c:param name="facet.field" value="datum" />
    <c:param name="facet.mincount" value="1" />
    <c:param name="f.datum.facet.sort" value="false" />
    <c:param name="fq" value="root_model:periodical" />
    <c:param name="fq" value="fedora.model:\"info:fedora/model:periodical\"" />
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
        <%@ include file="proccessFacets.jsp" %>
<%out.clear();%>({
"items": [<%

            Facet datumFacet = facets.get("datum");
            //jenom jeden, a prave 0. To nechceme, je default, kdyz neni datum.
            if ((datumFacet != null) &&
                    (datumFacet.infos.size() > 0) &&
                    !((datumFacet.infos.size() == 1) && (datumFacet.infos.get(0).displayName.equals("0")))) {

                String dateStr = "";

                for (int k = 0; k < datumFacet.infos.size(); k++) {
                    try {
                        int beginDate = Integer.parseInt(datumFacet.infos.get(k).displayName.split("-")[0].trim());
                        int endDate = Integer.parseInt(datumFacet.infos.get(k).displayName.split("-")[1].trim());
                        int diff = endDate - beginDate;
                        %>
{
    "beginDate": <%=beginDate%>,
    "endDate": <%=endDate%>,
    "count": <%=datumFacet.infos.get(k).count%>
}<% if(k < datumFacet.infos.size()-1) out.print(","); %><%
                    } catch (Exception ex) {
                        System.out.println(datumFacet.infos.get(k).displayName);
                        System.out.println(ex.toString());
                    } finally {
                        continue;
                    }
                }
            }
        %>
]
})
    </c:otherwise>
</c:choose>
