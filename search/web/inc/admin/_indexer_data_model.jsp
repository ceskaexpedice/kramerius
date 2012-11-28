<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="cz.incad.kramerius.resourceindex.*"%>

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

<%@ page isELIgnored="false"%>

<scrd:securedContent action="reindex" sendForbidden="true">

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="order" value="${param.sort}" />
<c:if test="${empty param.sort}">
    <c:set var="order" value="date" />
</c:if>
<c:set var="order_dir" value="${param.sort_dir}" />
<c:if test="${empty param.sort_dir}">
    <c:set var="order_dir" value="desc" />
</c:if>
        <%
    String rowsStr = request.getParameter("rows");
    int rows = 20;
    if(rowsStr!=null){
        rows = Integer.parseInt(rowsStr);
    }
    pageContext.setAttribute("rows", rows);
    

IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
String offsetStr = request.getParameter("offset");
int offset = 0;
if(offsetStr!=null){
    offset = Integer.parseInt(offsetStr);
}
String sort_dir = request.getParameter("sort_dir");
if(sort_dir==null){
    sort_dir = "desc";
}
String sort = request.getParameter("sort");
if("".equals(sort)){
    sort = null;
}
String selectedModel = request.getParameter("model");
if(selectedModel==null || selectedModel.length()==0){
    return;
}
org.w3c.dom.Document doc = g.getFedoraObjectsFromModelExt(selectedModel, rows+1, offset, sort, sort_dir);

pageContext.setAttribute("selModel", selectedModel);
pageContext.setAttribute("doc", doc);

    %>
<c:import url="indexer.xsl" var="xsltPage" charEncoding="UTF-8"  />
<x:transform doc="${doc}"  xslt="${xsltPage}"  >
    <x:param name="rows" value="${rows}" />
    <x:param name="offset" value="${param.offset}" />
    <x:param name="model" value="${selModel}" />
    <x:param name="sort" value="${order}" />
    <x:param name="sort_dir" value="${order_dir}" />
</x:transform>

</scrd:securedContent>
