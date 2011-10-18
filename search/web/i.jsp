<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="java.util.Locale"%>
<%@page import="com.google.inject.Provider"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang=" + lctx.getLocale().getLanguage() + "&country=" + lctx.getLocale().getCountry() + "&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<%@ include file="inc/checkUUID.jsp" %>
<%

            MostDesirable mostDesirable = ctxInj.getInstance(MostDesirable.class);
            mostDesirable.saveAccess(request.getParameter("pid"), new java.util.Date());
%>            
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<c:set var="title"><fmt:message bundle="${lctx}">application.title</fmt:message>. <x:out select="$doc/response/result/doc/str[@name='root_title']" /></c:set>
<html>
    <%@ include file="inc/html_header.jsp" %>
    <body>

        <div id="main" class="shadow">
            <%@ include file="inc/header.jsp" %>
            <div style="display:block;">
                <%@ include file="inc/details/item.jsp" %>
            </div>
            <div class="clear"></div>
        </div>
        <div id="fullImageContainer" class="viewer" style="display:none;"><%@ include file="inc/details/fullViewer.jsp" %></div>
        <div id="footer">
            <%@ include file="inc/footer.jsp" %>
        </div>
    
        <div id="i" class="viewer"></div>        
        
     </body>
</html>
