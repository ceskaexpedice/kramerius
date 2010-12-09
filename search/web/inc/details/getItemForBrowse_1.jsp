<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%@page import="com.google.inject.Injector, cz.incad.kramerius.service.*"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
            Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            
%>
<%@ include file="../initVars.jsp" %>
<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >
        parent_pid:"${param.pid}"<c:if test="${param.model!=null}"> and fedora.model:${param.model}</c:if>
    </c:param>
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="10000" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:param name="fl" value="PID,fedora.model,dc.title,details" />
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="fedora.model asc, rels_ext_index asc" />
    
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />
<%
            XSLService ts = (XSLService)inj.getInstance(XSLService.class);
            String xsl = request.getParameter("xsl");
            
            try {
                String text = ts.transform(xml, xsl);
                out.println(text);
            } catch (Exception e) {
                out.println(e);
            }
%>