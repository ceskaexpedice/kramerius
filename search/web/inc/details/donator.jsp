<%@page import="java.nio.charset.Charset"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.InputStream"%>
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
    if(donator!=null && !donator.equals("")){
        InputStream is = fedoraAccess.getDataStream("donator:"+donator, "TEXT-"+lctx.getLocale().getLanguage().toUpperCase());
        String donator_text = IOUtils.readAsString(is, Charset.forName("UTF-8"), true).trim();
        pageContext.setAttribute("donator", donator);
        pageContext.setAttribute("donator_text", donator_text);
    
%>
<img height="50" src="proxy?pid=donator:${donator}&dsname=LOGO" alt="${donator_text}" title="${donator_text}" />
<%
    }
%>