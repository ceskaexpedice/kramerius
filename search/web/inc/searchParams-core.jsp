<%@page import="cz.incad.kramerius.utils.UTFSort"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c-rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

<%--
<view:object name="searchParams" clz="cz.incad.Kramerius.views.inc.SearchParamsViews"></view:object>
--%>

<c:catch var="searchException">
    <c:set var="isCollapsed" value="${!isHome && (param.collapsed != 'false')}" scope="request"  />
    <c:set var="filterByType" value="false" scope="request" />
    <c:set var="rowsdefault" value="${searchParams.searchResultsRows}" scope="request" />
    <c:set var="rows" value="${rowsdefault}" scope="request" />
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:choose>
        <c:when test="${empty param.q}" >
            <c:param name="q" value="*:*" />
        </c:when>
        <c:when test="${param.q != null}" >
            <c:if test="${fn:containsIgnoreCase(param.q, '*')}" >
                
            </c:if>
            <c:choose>
                <c:when test="${param.asis}">
                    <c:param name="q" value="${param.q}" />
                </c:when>
                <c:otherwise><c:param name="q" value="${searchParams.escapedQuery}" /></c:otherwise>
            </c:choose>
            
            <c:set var="rows" value="${rowsdefault}" scope="request" />
        </c:when>

    </c:choose>
    <c:if test="${!empty param.fl}">
        <c:param name="fl" value="${param.fl}" />
    </c:if>
    <%--
    <c:param name="fl" value="PID,score,root_title,path,pid_path,root_pid,dc.title,details,fedora.model,model_path,dc.creator,datum,page_format,text" />
    --%>
    
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:if test="${fn:startsWith(fqs, 'document_type')}">
            <c:set var="isCollapsed" value="false" scope="request" />
            <c:set var="filterByType" value="true" scope="request" />
        </c:if>

        <c:param name="fq">${fqs}</c:param>
        <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:forEach>

    <%-- datum --%>
    <c:if test="${param.da_od != null && !empty param.da_od}">
        <c:set var="fieldedSearch" value="true" scope="request" />
        <c:param name="fq" value="(rok:[${searchParams.yearFrom} TO ${searchParams.yearUntil}]) OR (datum_begin:[1 TO ${searchParams.yearUntil}] AND datum_end:[${searchParams.yearFrom} TO 3000]) OR datum:[${searchParams.dateFromFormatted} TO ${searchParams.dateUntilFormatted}]" />
            <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:if>
    <c:if test="${!empty param.offset}">
        <c:param name="start" value="${param.offset}" />
    </c:if>
    
    <c:if test="${isCollapsed}">
        <%--
        <c:param name="collapse.field" value="root_pid" />
        <c:param name="collapse.type" value="normal" />
        <c:param name="collapse.threshold" value="1" />
        <c:param name="collapse.facet" value="before" />
        <c:param name="group.main" value="true" />
        --%>
        <c:param name="group.field" value="root_pid" />
        <c:param name="group.type" value="normal" />
        <c:param name="group.threshold" value="1" />
        <c:param name="group.facet" value="true" />
        <c:param name="group" value="true" />
        <c:param name="group.ngroups" value="true" />
    </c:if>

    <%-- suggest --%>
    <c:if test="${param.suggest}">
        <c:param name="fq" value="search_title:${searchParams.browserTitle}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:if>


    <c:set var="fieldedSearch" value="false" scope="request" />
    <%-- advanced params --%>
    <c:if test="${!empty param.issn}">
        <c:param name="fq" value="issn:${searchParams.escapedIssn} OR dc.identifier:${searchParams.escapedIssn}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.title}">
        <c:param name="fq" value="dc.title:${searchParams.escapedTitle}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.author}">
        <c:param name="fq" value="dc.creator:${searchParams.escapedAuthor}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.rok}">
        <c:param name="fq" value="rok:${searchParams.escapedRok}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.udc}">
        <c:param name="fq" value="mdt:${searchParams.escapedUdc}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.ddc}">
        <c:param name="fq" value="ddt:${searchParams.escapedDdc}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.keywords}">
        <c:param name="fq" value="keywords:${searchParams.escapedKeywords}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.onlyPublic}">
        <c:param name="fq" value="dostupnost:${param.onlyPublic}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>

    <view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
    <c:if test="${cols.current != null}">
        <c:param name="fq" value="collection:\"${cols.current.pid}\"" />
    </c:if>

   
    <c:if test="${isHome}" >
        <c:set var="rows" value="0" scope="request" />
    </c:if>
    <c:if test="${param.rows != null}" >
        <c:set var="rows" value="${param.rows}" scope="request" />
    </c:if>

    <c:param name="rows" value="${rows}" />
    <jsp:useBean id="rows" type="java.lang.String" scope="request" />

    <c:if test="${param.facet != 'false'}">
        <c:param name="facet.field" value="document_type" />
        <c:param name="facet.field" value="language" />
        <c:param name="facet.field" value="rok" />
        <c:param name="facet.field" value="keywords" />
        <c:param name="f.rok.facet.limit" value="-1" />
        <c:param name="f.rok.facet.sort" value="false" />
        <c:param name="facet" value="true" />
        <c:param name="facet.mincount" value="1" />
    </c:if>
    <c:if test="${rows!='0'}">
        <c:param name="facet.field" value="facet_autor" />
        <c:param name="facet.field" value="dostupnost" />
        <c:param name="f.facet_autor.facet.sort" value="false" />
    </c:if>

    <%-- Hit highlight --%>
    <c:if test="${!isHome && (param.hl != 'false')}">
    <c:param name="hl" value="true" />
    <c:param name="hl.fl" value="text_ocr" />
    <c:param name="hl.simple.pre" value="<span>" />
    <c:param name="hl.simple.post" value="</span>"  />
    <c:param name="hl.mergeContiguous" value="true" />
    <c:param name="hl.snippets" value="2" />
    </c:if>

    <%-- sort param --%>
    <c:choose>
        <c:when test="${param.sort != null && !empty param.sort && param.asis}" >
            <c:param name="sort" value="${param.sort}" />
        </c:when>
        <c:when test="${param.sort != null && !empty param.sort && filterByType}" >
            <c:param name="sort" value="${param.sort}" />
        </c:when>
        <c:when test="${param.sort != null && !empty param.sort}" >
            <c:param name="sort" value="level asc, ${param.sort}" />
        </c:when>
        <c:when test="${sort != null && !empty sort}" >
            <c:param name="sort" value="level asc, ${sort}" />
            <c:param name="group.sort" value="level asc, ${sort}" />
        </c:when>
        <c:when test="${filterByType && empty param.q}" >
            <c:param name="sort" value="title_sort asc" />
        </c:when>
        <c:when test="${filterByType && !empty param.q}" >
            <c:param name="sort" value="score desc, title_sort asc" />
        </c:when>
        <c:when test="${fieldedSearch}">
            <c:param name="sort" value="level asc, title_sort asc, score desc" />
        </c:when>
        <c:when test="${empty param.q}" >
            <c:param name="sort" value="level asc, title_sort asc, score desc" />
        </c:when>
        <c:otherwise>
            <c:param name="sort" value="level asc, score desc" />
        </c:otherwise>
    </c:choose>
    <c:param name="defType" value="edismax" />
</c:url>

    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
</c:catch>
<c:choose>
    <c:when test="${searchException!=null}">
        ${searchException}
        <%--<c:import url="empty.xml" var="xml" charEncoding="UTF-8" />--%>
        <c:set var="xml">
        <?xml version="1.0" encoding="UTF-8"?>
        <response>
            <lst name="responseHeader">
                <int name="status">1</int>
                <str name="error"><fmt:message bundle="${lctx}" key="search.error" /></str>
                <lst name="params">
                    <str name="q"></str>
                </lst>
            </lst>
            <result name="response" numFound="0" start="0"/>
        </response>
    </c:set>
        <x:parse var="doc" xml="${xml}"  />
    </c:when>
</c:choose>
<jsp:useBean id="xml" type="java.lang.String" />
<c:choose>
    <c:when test="${isCollapsed}"><c:set var="numDocs" scope="request" ><x:out select="$doc//response/lst[@name='grouped']/lst/int" /></c:set></c:when>
    <c:otherwise><c:set var="numDocs" scope="request" ><x:out select="$doc/response/result/@numFound" /></c:set></c:otherwise>
</c:choose>
<c:set var="numDocsCollapsed" scope="request" value="${0}" />
<x:forEach select="$doc/response/lst[@name='collapse_counts']/lst[@name='results']/lst">
    <c:set var="curCol"><x:out select="./int[@name='collapseCount']/text()"/></c:set>
    <c:set var="numDocsCollapsed" scope="request" value="${numDocsCollapsed + curCol}" />
</x:forEach>
<c:set var="numDocsStr" scope="request" >
    <c:choose>
        <c:when test="${numDocs==1}"><fmt:message bundle="${lctx}">common.documents.singular</fmt:message></c:when>
        <c:when test="${numDocs>1 && numDocs<5}"><fmt:message bundle="${lctx}">common.documents.plural_1</fmt:message></c:when>
        <c:when test="${numDocs>4}"><fmt:message bundle="${lctx}">common.documents.plural_2</fmt:message></c:when>
    </c:choose>
    (<c:out value="${numDocsCollapsed}" />)
</c:set>