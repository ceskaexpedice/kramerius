<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.service.*"%>
<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>

<c:set var="sort" scope="request">level asc, title asc</c:set>
<%@ include file="inc/searchParams-html.jsp" %>
<% out.clear(); %>
<%            
XSLService xs = (XSLService) ctxInj.getInstance(XSLService.class);

    try {
        String xsl = "uncollapsed.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/><c:out value="${exceptions}" />
</c:if>
<c:url var="xslPage" value="inc/results/xsl/uncollapsed.xsl" />
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <% out.clear();%>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2"> 
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="root_pid" value="${param.root_pid}"/>
                <x:param name="q" value="${param.q}"/>
            </x:transform>
            <c:set var="obj" value="#tabs_${param.level}" />
            <c:set var="href" value="#{href}" />
            <c:set var="label" value="#{label}" />
            <c:set var="target" value="#tab${label}-page" />
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>
<%@ include file="inc/paginationPageNum.jsp" %>