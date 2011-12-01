<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector, cz.incad.kramerius.service.*"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>

<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
	LocalizationContext lctx= ctxInj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="cs" lang="cs">
<%@ include file="inc/searchParams-html.jsp" %>
<c:if test="${param.debug}" >
    <c:out value="${url}" />
    <br/>
    <c:out value="${exceptions}" />
</c:if>
<jsp:useBean id="xml" type="java.lang.String" />
<%
            XSLService ts = (XSLService)ctxInj.getInstance(XSLService.class);
            String xsl = request.getParameter("xsl");

            try {
                String text = ts.transform(xml, xsl);
                out.println(text);
            } catch (Exception e) {
                out.println(e);
            }
%>