<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        pageContext.setAttribute("kconfig", kconfig);

%>
<view:kconfig var="newest_query" key="search.newest.query" defaultValue="level:0" />
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" value="${newest_query}" />
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="18" scope="request" />
        </c:otherwise>
    </c:choose>
    <view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
    <c:if test="${cols.current != null}">
        <c:param name="fq" value="collection:\"${cols.current.pid}\"" />
    </c:if>
    <c:param name="rows" value="${rows}" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
        <c:set var="filters" scope="request"><c:out value="${filters}" />&fq=<c:out value="${fqs}" /></c:set>
    </c:forEach>
    
    <c:if test="${!empty param.offset}">
        <c:param name="start" value="${param.offset}" />
    </c:if>
    <c:param name="sort" value="level asc, created_date desc" />
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
        
        <x:forEach varStatus="status" select="$doc/response/result/doc">
            <c:set var="pid"><x:out select="./str[@name='PID']"/></c:set>
            <c:set var="t"><x:out select="./str[@name='root_title']"/></c:set>
            <div align="center" style="overflow:hidden; border:1px solid #eeeeee; height:100px; width:100px; float:left; margin:5px; ">
                <a href="i.jsp?pid=${pid}" >
                    <img align="middle" vspace="2" id="img_${pid}" src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"
                         title="${t}" alt="${t}" />
                </a>
            </div>
        </x:forEach>
<div ><a href="inc/home/newest-rss.jsp"><span class="ui-icon ui-icon-signal-diag"></span></a></div>
    </c:otherwise>
</c:choose>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/>
    <c:out value="${param.parentPid}" />
</c:if>

