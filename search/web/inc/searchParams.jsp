<%@page import="cz.incad.kramerius.utils.UTFSort"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c-rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*" %>
<%
String search_results_rows = kconfig.getProperty("search.results.rows", "20");
pageContext.setAttribute("search_results_rows", search_results_rows);
%>
    <c:set var="rowsdefault" value="${search_results_rows}" scope="request" />
    <c:set var="rows" value="${rowsdefault}" scope="request" />
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
            <c:set var="rows" value="${rowsdefault}" scope="request" />
        </c:when>
        
    </c:choose>
    
    <c:if test="${param.facet != 'false'}">
        <c:param name="facet.field" value="document_type" />
        <c:param name="facet.field" value="language" />
        <c:param name="facet.field" value="rok" />
        <c:param name="f.rok.facet.limit" value="-1" />
        <c:param name="f.rok.facet.sort" value="false" />
        <c:param name="facet" value="true" />
        <c:param name="facet.mincount" value="1" />
    </c:if>
    <%--
    <c:param name="fl" value="PID,score,root_title,path,pid_path,root_pid,dc.title,details,fedora.model,model_path,dc.creator,datum,page_format,text" />
    --%>
    <c:set var="isCollapsed" value="${param.collapsed != 'false'}" /> 
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:if test="${fn:startsWith(fqs, 'document_type')}"><c:set var="isCollapsed" value="false" /></c:if>
         
        <c:param name="fq">${fqs}</c:param>
        <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:forEach>
    
    <%-- datum --%>
    <c:if test="${param.da_od != null && !empty param.da_od}">
<% 
    String rok_od = request.getParameter("da_od");
    rok_od = rok_od.substring(rok_od.lastIndexOf(".")+1);
    String rok_do = request.getParameter("da_do");
    rok_do = rok_do.substring(rok_do.lastIndexOf(".")+1);
    pageContext.setAttribute("rok_od", rok_od);
    pageContext.setAttribute("rok_do", rok_do);
    DateFormat df = new SimpleDateFormat(kconfig.getProperty("mods.date.format", "dd.MM.yyyy"));
    DateFormat dfout = new SimpleDateFormat(kconfig.getProperty("mods.date.format", "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'"));
    String datum_od = dfout.format(df.parse(request.getParameter("da_od"))) ;
    String datum_do = dfout.format(df.parse(request.getParameter("da_do"))) ;
    pageContext.setAttribute("datum_od", datum_od);
    pageContext.setAttribute("datum_do", datum_do);
%>
        <c:set var="fieldedSearch" value="true" scope="request" />
        <c:param name="fq" value="(datum_begin:[1 TO ${rok_od}] AND datum_end:[${rok_do} TO 3000]) OR datum:[${datum_od} TO ${datum_do}]" />
            <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:if>
    
    <c:param name="start" value="${param.offset}" />
    
    <c:if test="${isCollapsed}">
        <c:param name="collapse.field" value="root_pid" />
        <c:param name="collapse.type" value="normal" />
        <c:param name="collapse.threshold" value="1" />
        <c:param name="collapse.facet" value="before" />
    </c:if>
    
    <%-- suggest --%>
    <c:if test="${param.suggest}">
        <%
            String t = request.getParameter("browse_title");
            UTFSort utf_sort = new UTFSort();
            utf_sort.init();
            String browse_title = utf_sort.translate(t);
            browse_title = "\"" + browse_title + "##" + t + "\"";
            //browse_title = java.net.URLEncoder.encode(browse_title, "UTF-8");
            pageContext.setAttribute("browse_title", browse_title);
        %>
        <c:param name="fq" value="search_title:${browse_title}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
    </c:if>
    
    <c:set var="fieldedSearch" value="false" scope="request" />
    <%-- advanced params --%>
    <c:if test="${!empty param.issn}">
        <c:param name="fq" value="issn:${param.issn}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.title}">
        <c:param name="fq" value="dc.title:${param.title}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.author}">
        <c:param name="fq" value="dc.creator:${param.author}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.rok}">
        <c:param name="fq" value="rok:${param.rok}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.udc}">
        <c:param name="fq" value="udc:${param.udc}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.ddc}">
        <c:param name="fq" value="ddc:${param.ddc}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.keywords}">
        <c:param name="fq" value="keywords:${param.keywords}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
    <c:if test="${!empty param.onlyPublic}">
        <c:param name="fq" value="dostupnost:${param.onlyPublic}" />
        <c:set var="rows" value="${rowsdefault}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>

    <c:if test="${param.rows != null}" >
        <c:set var="rows" value="${param.rows}" scope="request" />
        <c:set var="fieldedSearch" value="true" scope="request" />
    </c:if>
        
    <c:param name="rows" value="${rows}" />
    <jsp:useBean id="rows" type="java.lang.String" scope="request" />
    <c:if test="${rows!='0'}">
        <c:param name="facet.field" value="facet_autor" />
        <c:param name="facet.field" value="dostupnost" />
        <c:param name="f.facet_autor.facet.sort" value="false" />
    </c:if>
    
    <%-- Hit highlight --%>
    <c:param name="hl" value="true" />
    <c:param name="hl.fl" value="text_ocr" />
    <c:param name="hl.simple.pre" value="<span>" />
    <c:param name="hl.simple.post" value="</span>"  />
    <c:param name="hl.mergeContiguous" value="true" />
    <c:param name="hl.snippets" value="2" />
    
    <%-- sort param --%>    
    <c:choose>
        <c:when test="${param.sort != null}" >
            <c:param name="sort" value="${param.sort}" />
        </c:when>
        <c:when test="${sort != null}" >
            <c:param name="sort" value="${sort}" />
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
</c:url>

<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<x:parse var="doc" xml="${xml}"  />
<c:set var="numDocs" scope="request" >
    <x:out select="$doc/response/result/@numFound" />
</c:set>
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