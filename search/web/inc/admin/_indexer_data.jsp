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

<scrd:securedContent action="display_admin_menu" sendForbidden="true">

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
    String[] models = kconfig.getPropertyList("fedora.topLevelModels");
    String selectedModel = request.getParameter("model");
    boolean canSort = Boolean.parseBoolean(kconfig.getProperty("search.index.canSort", "true"));
    
    if(selectedModel==null || selectedModel.length()==0){
        selectedModel = models[0];
    }
    pageContext.setAttribute("selModel", selectedModel);
    int rows = 40;
    pageContext.setAttribute("rows", rows);
    pageContext.setAttribute("top_models", models);
    pageContext.setAttribute("canSort", canSort);
%>
<style type="text/css">
    #indexerContent div.section{
        border-bottom:1px solid rgba(0, 30, 60, 0.9); 
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
        cursor: pointer;
    }
    .indexer_result_notindexed{
        background: url("img/alert.png") no-repeat;
        width:20px;
    }
    #indexer_tabs thead{
        width:100%;
        font-size: 11px;
    }
    #indexer_tabs thead span{
        float:left;
    }
    .indexer_result_status{
        min-width:16px;
    }
</style>
<div id="indexer_tabs">
    <ul>
        <li><a href="#indexer_browse_models"><fmt:message bundle="${lctx}">administrator.menu.dialogs.browse_fedora_models</fmt:message></a></li>
        <li><a href="#indexer_search_fedora"><fmt:message bundle="${lctx}">administrator.menu.dialogs.search_fedora</fmt:message></a></li>
        <li><a href="#indexer_other"><fmt:message bundle="${lctx}">administrator.menu.dialogs.other</fmt:message></a></li>
    </ul>
    <div id="indexer_other">       
        <div class="section">
            <fmt:message bundle="${lctx}">administrator.menu.dialogs.index_by_PID</fmt:message>: 
            <input type="text" id="pid_to_index" size="40" />
            <input type="button" onclick="confirmIndexDocByPid($('#pid_to_index').val(), '');" value="index_pid" class="ui-state-default ui-corner-all" />
        </div>
        <div class="section">
            <fmt:message bundle="${lctx}">administrator.menu.dialogs.check_integrity</fmt:message>&nbsp;
            <input type="button"  id="check_integrity" onclick="checkIndexIntegrity();" value="check" class="ui-state-default ui-corner-all" />    
        </div>
    </div>
    <div id="indexer_browse_models" class="indexer_data_container">  
        <div class="section">
            <fmt:message bundle="${lctx}">fedora.model</fmt:message>: 
            <%@include file="_indexer_models.jsp" %>&nbsp;
        <fmt:message bundle="${lctx}">administrator.menu.dialogs.rows</fmt:message>: <input type="text" id="doc_rows" value="50" size="4" style="text-align: right;" />
            <input type="button" onclick="confirmIndexModel($('#top_models_select').val());" value="<fmt:message bundle="${lctx}">administrator.menu.dialogs.index_model</fmt:message>" class="ui-state-default ui-corner-all" />
        </div> 
        <table id="indexer_data_model" cellpadding="0" cellspacing="0" class="indexer_selected" style="display:none;" width="100%">
            <thead class="indexer_head"><tr style="display:block;width:100%;">
                <th style="min-width:40px;"></th>
                <th width="100%" align="left">
                <fmt:message bundle="${lctx}">administrator.menu.dialogs.dc.title</fmt:message>
                </th>
                <th style="min-width:240px;" align="left">PID</th>
                <th style="min-width:138px;" align="left">
                    <input type="hidden" id="indexer_order_dir" value="${order_dir}" />
                    <input type="hidden" id="indexer_offset" value="0" />
                    <c:choose>
                        <c:when test="${canSort}">
                        <a href="javascript:orderDocuments('date')"><fmt:message>common.date</fmt:message></a>
                        </c:when>
                        <c:otherwise>
                            <fmt:message>common.date</fmt:message>
                        </c:otherwise>
                    </c:choose>
                    
                    <span id="date_order_arrow" class="ui-icon ui-icon-arrowthick-1-n">order</span>
                </th></tr></thead>
            <tbody style="overflow:auto;display:block;width:100%;"><tr><td align="center" colspan="3" width="768"><img src="img/loading.gif" /></td></tr></tbody>
            <tfoot class="indexer_head">
                <tr>
                <td width="100%" class="pager"  align="center">
                    <span class="prev"><a href="javascript:prevFedoraDocuments();">previous</a></span>&nbsp;&nbsp;&nbsp;
                    <span class="next"><a href="javascript:nextFedoraDocuments();">next</a></span>
                </td></tr>
            </tfoot>

        </table>
    </div>
    <div id="indexer_search_fedora" class="indexer_data_container">  
    
    <div class="section">
        <input id="search_fedora_text" type="text" />
        <a href="javascript:searchFedora(0);"><img border="0" align="top" src="img/lupa_orange.png" alt="Search"></a>&nbsp;
        <fmt:message bundle="${lctx}">administrator.menu.dialogs.rows</fmt:message>: <input type="text" id="indexer_search_doc_rows" value="25" size="4" style="text-align: right;" />
    </div>
    <table id="indexer_data_search" cellpadding="0" cellspacing="0" class="indexer_selected" style="display:none;" width="100%">
        <thead class="indexer_head"><tr style="display:block;width:100%;">
            <th style="min-width:40px;"></th>
            <th width="100%" align="left">
            <fmt:message bundle="${lctx}">administrator.menu.dialogs.dc.title</fmt:message>
            (<fmt:message bundle="${lctx}">document.type</fmt:message>)
            </th>
            <th style="min-width:240px;" align="left">PID</th>
            <th style="min-width:138px;" align="left"><span><fmt:message>common.date</fmt:message></span></th></tr></thead>
        <tbody style="overflow:auto;display:block;width:100%;">
            <tr>
                <td align="center" colspan="5" width="768"><img src="img/loading.gif" /></td>
            </tr>
        </tbody>
    </table>
    </div>
 
</div>  
<script type="text/javascript">
    $(document).ready(function(){
        $('.indexer_result_indexed').live('click', function(){
            var pid = $(this).parent().attr('pid').replace("info:fedora/", "");
            window.location = "i.jsp?pid="+pid;
        });
        $('#search_fedora_text').keypress(function(key){
            
            if( key.which == 13){
                searchFedora();
            }
        })
    });
    $('#indexer_tabs').tabs();
function prevFedoraDocuments(){
    var rows = parseInt($('#doc_rows').val());
    loadFedoraDocuments($('#top_models_select').val(), parseInt($('#indexer_offset').val())-rows, "");
}
function nextFedoraDocuments(){
    var rows = parseInt($('#doc_rows').val());
    loadFedoraDocuments($('#top_models_select').val(), parseInt($('#indexer_offset').val())+rows, "");
}
function orderDocuments(field){
    var sort_dir = $("#indexer_order_dir").val()=="asc"?"desc":"asc";
    $("#indexer_order_dir").val(sort_dir);
    loadFedoraDocuments($('#top_models_select').val(), 0, field);
}

function searchFedora(){
    $('#indexer_data_search').show();
    var rows = $('#indexer_search_doc_rows').val();
    var url = "inc/admin/_indexer_data_search.jsp?s="+$('#search_fedora_text').val()+"&rows="+rows;
    $("#indexer_data_search>tbody").html('<tr><td align="center" colspan="3" width="768"><img src="img/loading.gif" /></td></tr>');
    var diff = $("#indexer_search_fedora.indexer_data_container").outerHeight(true)
        - $("#indexer_search_fedora.indexer_data_container").height();
    var h = $("#indexer").height() - $("#indexer_tabs>ul").height() - diff - 12;
    $("#indexer_search_fedora.indexer_data_container").css("height", h);
    
    $.get(url, function(data) {
        $("#indexer_data_search>tbody>tr").remove();
        $("#indexer_data_search>tbody").css("height", h - $("#indexer_data_search>thead").height() - 25);
        $("#indexer_data_search>tbody").append(data);
        checkIndexed();
    });
}
function loadFedoraDocuments(model, offset, sort, rows){
    $('#indexer_data_model').show();
    if(!model) model = $('#top_models_select').val();
    if(!rows) rows = $('#doc_rows').val();
    
    var sort_dir = $("#indexer_order_dir").val()=="asc"?"asc":"desc";
    var url = "inc/admin/_indexer_data_model.jsp?model="+model+"&offset="+offset+"&sort="+sort+"&sort_dir="+sort_dir+"&rows="+rows;
    $("#indexer_data_model>tbody").html('<tr><td align="center" colspan="3" width="768"><img src="img/loading.gif" /></td></tr>');
    var diff = $("#indexer_browse_models.indexer_data_container").outerHeight(true)
        - $("#indexer_browse_models.indexer_data_container").height();
    var h = $("#indexer").height() - $("#indexer_tabs>ul").height() - diff - 12;
    
    $("#indexer_browse_models.indexer_data_container").css("height", h);
    $.get(url, function(data) {
        $("#indexer_data_model>tbody>tr").remove();
        $("#indexer_data_model>tbody").css("height", h - $("#indexer_data_model>thead").height()*2 - 25);
        $("#indexer_data_model>tbody").append(data);
        
        $("#indexer_order_dir").val(sort_dir);
        $("#indexer_offset").val(offset);
        if(sort_dir=="asc"){
            $("#date_order_arrow").removeClass('ui-icon-arrowthick-1-n');
            $("#date_order_arrow").addClass('ui-icon-arrowthick-1-s');
        }else{
            $("#date_order_arrow").removeClass('ui-icon-arrowthick-1-s');
            $("#date_order_arrow").addClass('ui-icon-arrowthick-1-n');
        }
        if(offset>0){
            $("#indexer_data_model .prev").show();
        }else{
            $("#indexer_data_model .prev").hide();
        }
        if($('#indexer_result_rows').length>0){
            $("#indexer_data_model .next").show();
        }else{
            $("#indexer_data_model .next").hide();
        }
        checkIndexed();
    });
}
        </script>
</scrd:securedContent>
