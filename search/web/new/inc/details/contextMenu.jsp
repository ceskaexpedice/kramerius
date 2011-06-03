<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager,cz.incad.kramerius.processes.DefinitionManager" %>
<%@page import="cz.incad.Kramerius.views.item.ItemViewObject"%>
<%@page import="cz.incad.Kramerius.views.item.menu.ContextMenuItem" %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration" %>
<%@page import="cz.incad.kramerius.security.SecuredActions" %>
<%
    ArrayList<ContextMenuItem> menus = new ArrayList<ContextMenuItem>();
    menus.add(new ContextMenuItem("administrator.menu.showmetadata", "", "viewMetadata", "", false));
    menus.add(new ContextMenuItem("administrator.menu.persistenturl", "", "persistentURL", "", false));
    menus.add(new ContextMenuItem("administrator.menu.generatepdf", "_data_x_role", "printMorePages", "", true));
    menus.add(new ContextMenuItem("administrator.menu.downloadOriginal", "_data_x_role", "downloadOriginal", "", true));
    
    if (request.getRemoteUser() != null) {
        menus.add(new ContextMenuItem("administrator.menu.reindex", "_data_x_role", "reindex", "", true));
        menus.add(new ContextMenuItem("administrator.menu.deletefromindex", "_data_x_role", "deletefromindex", "", true));
        menus.add(new ContextMenuItem("administrator.menu.deleteuuid", "_data_x_role", "deleteUuid", "", true));
        menus.add(new ContextMenuItem("administrator.menu.setpublic", "_data_x_role", "changeFlag", "", true));
        menus.add(new ContextMenuItem("administrator.menu.exportFOXML", "_data_x_role", "exportFOXML", "", true));
        menus.add(new ContextMenuItem("administrator.menu.exportcd", "_data_x_role", "generateStatic",
            "'static_export_CD','img','"+i18nServlet+"','"+lctx.getLocale().getISO3Country()+"','"+lctx.getLocale().getISO3Language()+"'", true));

        menus.add(new ContextMenuItem("administrator.menu.exportdvd", "_data_x_role", "generateStatic",
            "'static_export_CD','img','"+i18nServlet+"','"+lctx.getLocale().getISO3Country()+"','"+lctx.getLocale().getISO3Language()+"'", true));
        menus.add(new ContextMenuItem("administrator.menu.generateDeepZoomTiles", "_data_x_role", "generateDeepZoomTiles", "", true));
        menus.add(new ContextMenuItem("administrator.menu.deleteGeneratedDeepZoomTiles", "_data_x_role", "deleteGeneratedDeepZoomTiles", "", true));
    
        menus.add(new ContextMenuItem("administrator.menu.showrights", "_data_x_role", "securedActionsTableForCtxMenu", 
            "'"+SecuredActions.READ.getFormalName()+"', '" + SecuredActions.ADMINISTRATE.getFormalName()+"'", true));
         menus.add(new ContextMenuItem("administrator.menu.editor", "_data_x_role", "openEditor",
                 "'" + kconfig.getEditorURL() + "'", true));
         }
%>
<div>Scope</div>
<div class="scope selected viewer" id="scope_single"><fmt:message bundle="${lctx}">administrator.menu.active</fmt:message>
<ul id="context_items_active"></ul>
</div>
<div class="scope" id="scope_multiple"><fmt:message bundle="${lctx}">administrator.menu.selected.scope</fmt:message>
<ul id="context_items_selection"></ul>
</div>
<div style="height:0px;border-top:1px solid silver;"></div>
<div>Actions: 
<ul id="contextMenuList">
<%
    for(ContextMenuItem menu : menus){
%>
<li <%=menu.supportMultiple ? "class=\"multiple\"": ""%> ><span class="ui-icon ui-icon-triangle-1-e  " >item</span>
    <a title="<fmt:message bundle="${bundle}"><%=menu.key%></fmt:message>" href="javascript:<%=menu.jsFunction%>(<%=menu.jsArgs%>);"><fmt:message bundle="${bundle}"><%=menu.key%></fmt:message></a>
</li>
<%
    }
%>
</ul>
</div>
<script type="text/javascript">
    
    $(document).ready(function(){
        $('#contextMenuList>li>a').click(function(e) {
            if($(this).parent().hasClass('disabled')){
                e.preventDefault();
            }
        });
        $('#contextMenu>div.scope').click(function(e) {
            var scope = $(this).attr('id').split('_')[1];
            if(scope=="multiple"){
                $('#contextMenuList>li.multiple').addClass('disabled');
            }else{
                $('#contextMenuList>li.multiple').removeClass('disabled');
            }
            $('#contextMenu>div.scope').removeClass('selected');
            $(this).addClass('selected');
        });

        $('#scope_single.viewer').bind('viewReady', function(event, id){
            var t = '<li><span class="ui-icon ui-icon-triangle-1-e " >item</span>'+$("#"+k4Settings.activeUuid+">a").html()+'</li>';
            $('#context_items_active').html(t);
        });

    });

    function getTreeSelection(){
        var uuids = [];
        $('#item_tree input:checked').each(function(){
            var id = $(this).parent().attr("id");
            uuids.push(id);
        });
        return uuids;
    }

    function getTreeActiveUuid(){
        return k4Settings.activeUuid;
    }

    /*
     * returns 'single' or 'multiple'
     */
    function getScope(){
        return $('#contextMenu>div.selected').attr('id').split('_')[1];
    }

    function viewMetadata(){
        
    }

    var _persistentURLDialog;
    function persistentURL(){
        var currentURL = window.location.href;
        if (currentURL.match("^https")=='https') {
            currentURL = currentURL.substr('https://'.length, currentURL.length);
            var urlparts = currentURL.split('/');
            currentURL="https://"+urlparts[0]+"/"+urlparts[1]+"/";
        } else {
            currentURL = currentURL.substr('http://'.length, currentURL.length);
            var urlparts = currentURL.split('/');
            currentURL="http://"+urlparts[0]+"/"+urlparts[1]+"/";
        }

        var textFieldID = 'persistentURLTextField';

        if (_persistentURLDialog) {
            _persistentURLDialog.dialog('open');
        } else {
            $(document.body).append('<div id="persistentURL">'+
                '<span>'+dictionary['administrator.dialogs.persistenturl.text']+'</span>'+
                '<div id="'+textFieldID+'" ></div>' +
                //'<input name="'+textFieldID+'"  style="width:100%;" type="text"  maxlength="255"'+
                //' id="'+textFieldID+'" title="'+dictionary['administrator.menu.persistenturl']+'" />'+
                '</div>');

            _persistentURLDialog = $('#persistentURL').dialog({
                width:640,
                //height:100,
                modal:true,
                title:dictionary["administrator.menu.dialogs.persistenturl.title"],
                buttons: {
                    "Close": function() {
                        $(this).dialog("close");
                    }
                }
            });
        }
        
        if(getScope()=='single'){
            var input = '<input name="'+textFieldID+'"  style="width:100%;" type="text" '+
                ' id="'+textFieldID+'" value="'+currentURL+"handle/uuid:"+k4Settings.activeUuid+'"  />';
            $('#'+textFieldID).html(input);
            $('#'+textFieldID+'>input').select();
        
        }else{
            var uuids = getTreeSelection();
            var input;
            $('#'+textFieldID).html('');
            for(var i=0; i<uuids.length; i++){
                input = $("#"+uuids[i]+">a").html() + ': <input name="'+textFieldID+'"  style="width:100%;" type="text" '+
                    ' id="'+textFieldID+'" value="'+currentURL+"handle/uuid:"+uuids[i]+'"  />';
                $('#'+textFieldID).append(input);
            }
        }
        $('#'+textFieldID+'>input').focus(function() {
            $(this).select();
        });
        
    }
    
    function printMorePages(){
        alert("printMorePages");
    }
    
    function downloadOriginal(){
        
    }
<% 
if (request.getRemoteUser() != null) {
%>
<% 
}
%>
    
</script>