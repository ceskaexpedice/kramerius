<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="com.google.inject.Injector"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%
	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
	TextsService ts = (TextsService)inj.getInstance(TextsService.class);	


	String lang = request.getParameter("language");
    if (lang == null || lang.length() == 0) {
        lang = "cs";
    }
    try {
        String text = ts.getText("help", ts.findLocale(lang));
        out.println(text);
    } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println("Loading default");
%><c:choose>
    <c:when test="${param.language == 'en'}"><%@ include file="en.html" %></c:when>
    <c:when test="${param.language == 'cs'}"><%@ include file="cs.html" %></c:when>
    <c:otherwise><%@ include file="cs.html" %></c:otherwise>
</c:choose><%
    }
%>
