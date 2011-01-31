<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page trimDirectiveWhitespaces="true"%>

<%@page import="javax.servlet.jsp.jstl.core.Config"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="com.google.inject.Injector, cz.incad.kramerius.resourceindex.*"%>
<%@ page isELIgnored="false"%>

<%@ include file="../inc/initVars.jsp" %>
<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<c:set var="order" value="${param.sort}" />
<c:if test="${empty param.sort}">
    <c:set var="order" value="title" />
</c:if>
<c:set var="order_dir" value="${param.sort_dir}" />
<c:if test="${empty param.sort_dir}">
    <c:set var="order_dir" value="asc" />
</c:if>
<table cellpadding="0" cellspacing="0" border="0"  width="100%">
    <tr>
        <td valign="top"><div  class="indexer_models">
                <table width="100%" cellpadding="0" cellspacing="0" border="0" id="indexerModels"><tr><td colspan="3">Top level models:</td></tr>
    <%
    //String[] models = kconfig.getProperty("fedora.topLevelModels").split(",");
    String[] models = kconfig.getPropertyList("fedora.topLevelModels");
    String selectedModel = request.getParameter("model");
    if(selectedModel==null || selectedModel.length()==0){
        selectedModel = models[0];
    }
    pageContext.setAttribute("selModel", selectedModel);
    int rows = 10;
    pageContext.setAttribute("rows", rows);
    for(String model:models){
        String css = "";
        if(model.equals(selectedModel)) css = "class=\"indexer_selected\"";
    %>
    <tr <%=css%>><td><a href="javascript:loadFedoraDocuments('<%=model%>', 0, '<c:out value="${order}" />', '<c:out value="${order_dir}" />');"><%=model%></a></td>
    <td valign="middle" width="20"><a href="javascript:loadFedoraDocuments('<%=model%>', 0, '<c:out value="${order}" />', '<c:out value="${order_dir}" />' );" title="select model"><img src="img/lupa_orange.png" border="0" alt="find" /></a></td>
    <td valign="middle" width="20"><a href="javascript:indexModel('<%=model%>');" title="index model"><img src="img/reindex.png" alt="reindex" border="0" /></a></td>
    </tr>
    <%
    }
    %>
</table></div></td>
<td valign="top"><div class="indexer_selected">
<%
IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
String offsetStr = request.getParameter("offset");
int offset = 0;
if(offsetStr!=null){
    offset = Integer.parseInt(offsetStr);
}
String sort_dir = request.getParameter("sort_dir");
if(sort_dir==null){
    sort_dir = "asc";
}
org.w3c.dom.Document doc = g.getFedoraObjectsFromModelExt(selectedModel, rows, offset, "date", sort_dir);

pageContext.setAttribute("doc", doc);
%>
<table cellpadding="0" cellspacing="0" class="indexer_selected"  width="100%">
    <thead class="indexer_head ui-dialog-titlebar"><tr>
        <td></td><td><fmt:message>filter.query.title</fmt:message></td>
        <td width="138">
            <c:choose>
                <c:when test="${order_dir == 'desc'}">
                    <a href="javascript:loadFedoraDocuments('<c:out value="${selModel}" />', 0, 'date', 'asc')"><fmt:message>common.date</fmt:message></a>
                    <span class="ui-icon indexer_order_down">title</span>
                </c:when>
                <c:otherwise>
                    <a href="javascript:loadFedoraDocuments('<c:out value="${selModel}" />', 0, 'date', 'desc')"><fmt:message>common.date</fmt:message></a>
                    <span class="ui-icon indexer_order_up">title</span>
                </c:otherwise>
            </c:choose>
        </td></tr></thead>
<c:import url="indexer.xsl" var="xsltPage" charEncoding="UTF-8"  />
<x:transform doc="${doc}"  xslt="${xsltPage}"  >
    <x:param name="rows" value="${rows}" />
    <x:param name="offset" value="${param.offset}" />
    <x:param name="model" value="${selModel}" />
    <x:param name="sort" value="${order}" />
    <x:param name="sort_dir" value="${order_dir}" />
</x:transform>
</table></div></td></tr></table>