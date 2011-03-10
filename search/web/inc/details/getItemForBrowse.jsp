<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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
    <c:param name="fl" value="PID,fedora.model,dc.title,details,page_format" />
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="fedora.model asc, rels_ext_index asc" />
    <c:param name="fq" >
        NOT(PID:${param.pid}/@*)
    </c:param>
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />
<% 
cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    try {
        String xsl = "rightMenu.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl);
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:url var="xslPage" value="xsl/rightMenu.xsl" />
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
    <% out.clear();%>
    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="pid" value="${param.pid}"/>
        <x:param name="level" value="${param.level}"/>
        <x:param name="onlyrels" value="${param.onlyrels}"/>
        <x:param name="onlyinfo" value="${param.onlyinfo}"/>
    </x:transform>
    <c:set var="obj" value="#tabs_${param.level}" />
    <c:set var="href" value="#{href}" />
    <c:set var="label" value="#{label}" />
    <c:set var="target" value="#tab${label}-page" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
</c:choose>

