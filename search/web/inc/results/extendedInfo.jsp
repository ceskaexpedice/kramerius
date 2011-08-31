<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%
    Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
    KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
    pageContext.setAttribute("kconfig", kconfig);
    LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
    pageContext.setAttribute("lctx", lctx);
    String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
    pageContext.setAttribute("i18nServlet", i18nServlet);
    cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    String xsl = "extInfo.xsl";
%>
<c:set var="q"></c:set>
<c:forTokens var="pid" varStatus="status" delims="/" items="${param.pid_path}">        
<c:if test="${not(status.first)}">
    <c:set var="q">${q} OR</c:set>
</c:if> 
    <c:set var="q">${q} PID:"${pid}"</c:set>
</c:forTokens>
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" value="${q}" />
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />

<%
    try {
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:url var="xslPage" value="xsl/extInfo.xsl" />
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
    <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="pid" value="${pid}"/>
    </x:transform>
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
</c:choose>
