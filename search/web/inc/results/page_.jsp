<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />

<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />
<c:set var="pidQuery">
rdf.kramerius.hasPage:"info:fedora/<c:out value="${param.PIDPage}" />"
</c:set>
    <c:url var="url2" value="http://194.108.215.227:8080/solr/select/select" >
        <c:param name="q" value="${pidQuery}" />
    </c:url>
    <c:catch var="exceptions"> 
        <c:import url="${url2}" var="xml2" charEncoding="UTF-8" />
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            <c:out value="${exceptions}" /><br/><br/>
        </c:when>
        <c:otherwise>
            <c:catch var="exceptions2"> 
                <x:parse var="doc2" xml="${xml2}"  />
            </c:catch>
            <c:if test="${exceptions3 != null}" >
                exception2 <c:out value="${exceptions3}" /><br/>
            </c:if>
            <c:set var="numDocs2" scope="request" >
                <x:out select="$doc2/response/result/@numFound" />
            </c:set>
            <x:forEach select="$doc2/response/result/doc">
            <tr>
                <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
                <td class="textpolestrong">*</td>
                <td>
                    <a href="<c:out value="${fedoraHost}" />/get/<c:out value="${param.PIDPage}"/>"><x:out select="./arr[@name='dc.title']"/></a><br/>
                    <fmt:message bundle="${lctx}">info:fedora/model:page</fmt:message>: <c:out value="${param.title_orig}"/>
                </td>
                <td class="textpole">()</td>
            </tr>
            </x:forEach>
        </c:otherwise>
    </c:choose>