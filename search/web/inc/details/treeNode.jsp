<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page trimDirectiveWhitespaces="true" %>
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
<view:kconfig var="policyPublic" key="search.policy.public" defaultValue="true" />
<c:set var="escaped_pid">\:</c:set>
<c:set var="escaped_pid">${fn:replace(param.pid, ":" , escaped_pid)}</c:set>

<c:set var="q">parent_pid:"${param.pid}" AND NOT(PID:"${param.pid}")</c:set>
<c:if test="${param.model!=null}">
    <c:set var="q"> ${q} AND fedora.model:${param.model}</c:set>
</c:if>
<view:object name="cols" clz="cz.incad.Kramerius.views.virtualcollection.VirtualCollectionViewObject"></view:object>
<c:if test="${cols.current != null}">
    <c:set var="q"> ${q} AND collection:"${cols.current.pid}"</c:set>
</c:if>
<c:url var="url" value="${kconfig.solrHost}/select" >
    <c:param name="q" value="${q}" />
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="10000" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:if test="${!empty param.offset}">
        <c:param name="start" value="${param.offset}" />
    </c:if>
<%--    <c:param name="sort" value="fedora.model asc" /> --%>
    <c:param name="fq" >
        NOT(PID:${escaped_pid}/@*)
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
            if(request.getParameter("onlyrels")!=null){
                params.put("onlyrels", request.getParameter("onlyrels"));
            }
            if(request.getParameter("onlyinfo")!=null){
                params.put("onlyinfo", request.getParameter("onlyinfo"));
            }
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
    <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
        <x:param name="bundle_url" value="${i18nServlet}"/>
        <x:param name="pid" value="${param.pid}"/>
        <x:param name="model_path" value="${param.model_path}"/>
        <x:param name="onlyrels" value="${param.onlyrels}"/>
        <x:param name="onlyinfo" value="${param.onlyinfo}"/>
        <x:param name="policyPublic" value="${policyPublic}"/>
    </x:transform>
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
<c:if test="${param.debug =='true'}"><c:out value="${url}" />
<br/>
i18nServlet: ${i18nServlet}<br/>
pid: ${param.pid}<br/>
level: ${param.level}<br/>
onlyrels: ${param.onlyrels}<br/>
onlyinfo: ${param.onlyinfo}<br/>
xslPage ${xslPage}<br/>
<c:out value="${url}" /><br/>
<c:out value="${xml}" /><br/>
</c:if>
    </c:when>
</c:choose>