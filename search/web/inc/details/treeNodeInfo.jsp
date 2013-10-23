<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="java.util.Map,java.util.HashMap"%>
<%
            Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
            KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
            pageContext.setAttribute("kconfig", kconfig);
            LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
            pageContext.setAttribute("lctx", lctx);
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
%>
<view:kconfig var="policyPublic" key="search.policy.public" defaultValue="false" />
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" >
        PID:"${param.pid}"
    </c:param>
    
</c:url>
<c:import url="${url}" var="xml" charEncoding="UTF-8" />
<jsp:useBean id="xml" type="java.lang.String" />

<%
cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    try {
        String xsl = "treeNode.xsl";
        if (xs.isAvailable(xsl)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("pid", request.getParameter("pid"));
            if(request.getParameter("model_path")!=null){
                params.put("model_path", request.getParameter("model_path"));
            }
            params.put("onlyinfo", "true");
            if(request.getParameter("policyPublic")!=null){
                params.put("policyPublic", request.getParameter("policyPublic"));
            }
            String text = xs.transform(xml, xsl, lctx.getLocale(), params);
            out.println(text);
            return;
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
<c:url var="xslPage" value="xsl/treeNode.xsl" />
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
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2"> 
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="bundle_url" value="${i18nServlet}"/>
                <x:param name="pid" value="${param.pid}"/>
                <x:param name="model_path" value="${param.model_path}"/>
                <x:param name="onlyinfo" value="true"/>
                <x:param name="policyPublic" value="${policyPublic}"/>
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

