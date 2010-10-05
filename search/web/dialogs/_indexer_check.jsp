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

<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >
        PID:"${param.pid}"
    </c:param>
    <c:param name="rows" value="0" />
    <c:param name="fl" value="fedora.model" />
    
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
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <% out.clear();%><x:out select="$doc/response/result/@numFound" />    
    </c:otherwise>
</c:choose>