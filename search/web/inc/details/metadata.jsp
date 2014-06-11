<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="java.util.Map,java.util.HashMap"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            org.w3c.dom.Document xml = fedoraAccess.getBiblioMods(request.getParameter("pid"));

    cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    try {
        String xsl = "mods.xsl";
        if (xs.isAvailable(xsl)) {
            Map<String, String> params = new HashMap<String, String>();
            if(request.getParameter("pid")!=null){
                params.put("pid", request.getParameter("pid"));
            }
            if(request.getParameter("model")!=null){
                params.put("model", request.getParameter("model"));
            }
            String text = xs.transform(xml, xsl, lctx.getLocale(), params);
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
    pageContext.setAttribute("xml", xml);

%>
<c:set var="xsl" value="xsl/mods.xsl" scope="request" />
<c:url var="xslPage" value="${xsl}" >
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}" >
        <jsp:useBean id="exceptions" type="java.lang.Exception" />
        <% System.out.println(exceptions); %>
    </c:when>
    <c:otherwise>
        <c:catch var="exceptions2"> 
            <% out.clear(); %>
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="pid" value="${param.pid}"/>
                <x:param name="bundle_url" value="${i18nServlet}"/>
                <x:param name="model" value="${param.model}"/>
            </x:transform>
        </c:catch>
        <c:choose>
            <c:when test="${exceptions2 != null}" >
                <jsp:useBean id="exceptions2" type="java.lang.Exception" />
                <% System.out.println(exceptions2); %>
            </c:when>
            <c:otherwise></c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>