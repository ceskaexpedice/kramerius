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

<scrd:securedContent action="reindex">

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
<%
    String[] models = kconfig.getPropertyList("fedora.topLevelModels");
    String selectedModel = request.getParameter("model");
    if(selectedModel==null || selectedModel.length()==0){
        selectedModel = models[0];
    }
    pageContext.setAttribute("selModel", selectedModel);
    int rows = 10;
    pageContext.setAttribute("rows", rows);
    pageContext.setAttribute("top_models", models);
%>
<style type="text/css">
    #indexerContent>div.section{
        border-bottom:1px solid #E66C00; 
        padding-bottom: 5px; 
        margin-bottom: 5px;
    }
    .indexer_result{
        border-bottom:1px solid silver;
        background:#eeeeee;
    }
    .indexer_result td{
        border-bottom:1px solid silver;
        padding-left:10px;
        padding-top: 3px;
        padding-bottom: 2px;
    }
    .indexer_result_indexed{
        background: url("img/ok.png") no-repeat;
        width:20px;
    }
    .indexer_result_notindexed{
        background: url("img/alert.png") no-repeat;
        width:20px;
    }
    #indexer_data_model>thead{
        width:100%;
        font-size: 11px;
    }
    #indexer_data_model>thead span{
        float:right;
    }
</style>
<div class="section">
    <fmt:message bundle="${lctx}">administrator.menu.dialogs.index_by_PID</fmt:message>: 
    <input type="text" id="pid_to_index" size="40" />
    <input type="button" onclick="confirmIndexDocByPid($('#pid_to_index').val(), '');" value="index_pid" class="ui-state-default ui-corner-all" />
</div>
<div class="section">
    <fmt:message bundle="${lctx}">administrator.menu.dialogs.check_integrity</fmt:message>&nbsp;
    <input type="button"  id="check_integrity" onclick="checkIndexIntegrity();" value="check" class="ui-state-default ui-corner-all" />    
</div>
<div id="indexer_data_container">   
<div>
    <fmt:message bundle="${lctx}">administrator.menu.dialogs.browse_fedora_top_models</fmt:message>: 
    <select id="top_models_select" onChange="loadFedoraDocuments($('#top_models_select').val(), 0, '${order}', '${order_dir}' );">
        <option>--</option>
        <c:forEach var="top_model" items="${top_models}">
        <option value="${top_model}">${top_model}</option>
        </c:forEach>
    </select>
    <input type="button" onclick="confirmIndexModel($('#top_models_select').val());" value="<fmt:message bundle="${lctx}">administrator.menu.dialogs.index_model</fmt:message>" class="ui-state-default ui-corner-all" />
</div> 
<table id="indexer_data_model" cellpadding="0" cellspacing="0" class="indexer_selected"  width="100%">
    <thead class="indexer_head"><tr style="display:block;width:100%;">
        <th width="20px"></th>
        <th width="510px" align="left"><fmt:message bundle="${lctx}">administrator.menu.dialogs.dc.title</fmt:message></th>
        <th width="138px">
            <c:choose>
                <c:when test="${order_dir == 'desc'}">
                    <a href="javascript:loadFedoraDocuments('${selModel}', 0, 'date', 'asc')"><fmt:message>common.date</fmt:message></a>
                    <span class="ui-icon indexer_order_down">title</span>
                </c:when>
                <c:otherwise>
                    <a href="javascript:loadFedoraDocuments('${selModel}', 0, 'date', 'desc')"><fmt:message>common.date</fmt:message></a>
                    <span class="ui-icon indexer_order_up">title</span>
                </c:otherwise>
            </c:choose>
        </th></tr></thead>
    <tbody style="overflow:auto;display:block;width:100%;"></tbody>
</table>
</div>    
        <script type="text/javascript">
            
function loadFedoraDocuments(model, offset, sort, sort_dir){
    var url = "inc/admin/_indexer_data_model.jsp?model="+model+"&offset="+offset+"&sort="+sort+"&sort_dir="+sort_dir;
    $.get(url, function(data) {
        $("#indexer_data_model>tbody>tr").remove();
        var h = $("#indexer").height();
        $("#indexerContent>div.section").each(function(){
            h = h - $(this).height() - 11;
        });
        $("#indexer_data_container").css("height", h);
        $("#indexer_data_model>tbody").css("height", h - $("#indexer_data_model>thead").height() - 25);
        $("#indexer_data_model>tbody").append(data);
        checkIndexed();
    });
}
        </script>
</scrd:securedContent>
