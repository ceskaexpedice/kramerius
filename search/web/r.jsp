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
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            XSLService xs = (XSLService) ctxInj.getInstance(XSLService.class);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="inc/searchParams.jsp" %>
<c:set var="title"><fmt:message bundle="${lctx}">application.title</fmt:message>. <fmt:message bundle="${lctx}">search.results</fmt:message></c:set>
<c:choose>
    <c:when test="${param.onlymore}">
        <%@ include file="inc/results/docs.jsp" %>
    </c:when>
    <c:otherwise>
<html>
    <%@ include file="inc/html_header.jsp" %>
    <body>
        <div id="main" class="shadow">
            <%@ include file="inc/header.jsp" %>
            <div class="clear"></div>

            <div id="split" style="display:block;">
                <%@ include file="inc/results/results.jsp" %>
            </div>
            <div class="clear"></div>
        </div>
        <div id="footer">
            <%@ include file="inc/footer.jsp" %>
        </div>
    </body>
</html>

    </c:otherwise>
</c:choose>
