<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.*"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>
<%
    Injector inj = (Injector) application.getAttribute(Injector.class.getName());
    LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
    FedoraAccess fedoraAccess = inj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
    pageContext.setAttribute("lctx", lctx);
    String donator = fedoraAccess.getDonator(request.getParameter("uuid"));
    out.clear();
    out.print(donator);
%>