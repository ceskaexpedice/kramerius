<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*" %>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<%
if(session.getAttribute("configuration")==null)
{
        String configfile = getServletContext().getRealPath("WEB-INF/config/config.xml");
        KConfiguration kconfig =  new KConfiguration(configfile);
	session.setAttribute( "configuration", kconfig);
	session.setAttribute( "fedoraSolr", kconfig.getProperty("fedoraSolr") );
	session.setAttribute( "fedoraHost", kconfig.getProperty("fedoraHost"));
}
KConfiguration kconfig = (KConfiguration) getServletContext().getAttribute("configuration");

//c:url var="url" value="http://localhost:8983/solr/select/select" 
//http://194.108.215.227:8080/solr/select?indent=on&version=2.2&q=fedora.model%3A%22info%3Afedora%2Fmodel%3Apage%22&start=0&rows=10&fl=*%2Cscore&qt=standard&wt=xslt&explainOther=&hl.fl=&facet=true&facet.field=fedora.model&tr=example.xsl

//<c:set var="fedoraSolr" value="http://194.108.215.227:8080/solr/select/select" />
//<c:set var="fedoraHost" value="http://194.108.215.227:8080/fedora" />

%> 
<c:set var="pageType" value="search" />
<jsp:useBean id="pageType" type="java.lang.String" />
<c:url var="url" value="${fedoraSolr}" >
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:when test="${empty param.fq && empty param.q && param.f1 == null}">
            <c:set var="rows" value="0" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="20" scope="request" />
        </c:otherwise>
    </c:choose>
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
    <c:param name="rows" value="${rows}" />
    <c:param name="facet.field" value="fedora.model" />
    <%--
    <c:param name="f.fedora.model.facet.sort" value="false" />
    --%>
    <c:param name="facet.field" value="path" />
    <c:param name="f.path.facet.sort" value="false" />
    <c:param name="facet.field" value="language" />
    
    <c:param name="facet.field" value="rok" />
    <c:param name="f.rok.facet.limit" value="-1" />
    <c:param name="f.rok.facet.sort" value="false" />
    
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
    </c:forEach>
    <c:if test="${param.f1 != null}">
        <c:param name="fq" value="rok:[${param.f1} TO ${param.f2}]" />
    </c:if>
    <c:param name="start" value="${param.offset}" />
    <c:choose>
        <c:when test="${param.sort != null}" >
            <c:param name="sort" value="${param.sort}" />
        </c:when>
        <c:otherwise>
            <c:param name="sort" value="level asc, score desc" />
        </c:otherwise>
    </c:choose>
    
</c:url>

<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:if test="${exceptions != null}" >
    
    <c:import url="empty.xml" var="xml" />
</c:if>
<x:parse var="doc" xml="${xml}"  />
<c:set var="numDocs" scope="request" >
    <x:out select="$doc/response/result/@numFound" />
</c:set>