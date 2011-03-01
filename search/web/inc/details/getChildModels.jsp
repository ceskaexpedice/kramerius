<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
            Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
%>
<%@ include file="../initVars.jsp" %>

<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >
        parent_pid:"${param.pid}"
    </c:param>
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="10000" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="collapse.field" value="fedora.model" />
    <c:param name="rows" value="${rows}" />
    <c:param name="fl" value="fedora.model" />
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="fedora.model asc" />
    <c:param name="fq" >
        NOT(parent_pid:${param.pid}/@*)
    </c:param>
    
</c:url>

<c:url var="xslPage" value="xsl/relsextDetails.jsp" />
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
    <x:parse var="doc" xml="${xml}"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <% out.clear();%>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <x:if select="$doc/response/result/@numFound > 0" >
            [<x:forEach varStatus="status" select="$doc/response/result/doc/str">
                '<x:out select="."/>'<c:if test="${not status.last}">,</c:if>
            </x:forEach>]
        </x:if>    
    </c:otherwise>
</c:choose>

