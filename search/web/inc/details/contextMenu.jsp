<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

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

<view:object name="ctxMenu" clz="cz.incad.Kramerius.views.item.menu.ContextMenuItemsHolder"></view:object>

<style type="text/css">

    #contextMenu{
        margin: 2px;
        padding-left: 4px;
        padding-right: 4px;
    }
    #contextMenu ul{
        margin: 2px;
        padding-left: 4px;
    }
    #contextMenu li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 16px;
    }

    #contextMenu li>span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }
    
    #contextMenu .scope>span{
        font-weight: bold;
    }
    
    
    #reindex ul{
        margin: 2px;
        padding-left: 4px;
    }
    #reindex li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 16px;
    }

    #reindex li>span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }
    
    #reindex .scope>span{
        font-weight: bold;
    }
    #reindex>div.allowed{
        border-bottom:1px solid #E66C00; 
        margin-bottom:5px;
        padding-bottom:3px;
    }
</style>
<div><h3><fmt:message bundle="${lctx}">administrator.menu.Scope</fmt:message>:</h3></div>
<div class="scope selected viewer" id="scope_single"><span><fmt:message bundle="${lctx}">administrator.menu.active</fmt:message></span>
    <ul id="context_items_active"></ul>
</div>
<div class="scope" id="scope_multiple"><span><fmt:message bundle="${lctx}">administrator.menu.selected.scope</fmt:message></span>
    <ul id="context_items_selection"></ul>
</div>
<div style="height:0px;border-top:1px solid silver;"></div>
<div><h3><fmt:message bundle="${lctx}">administrator.menu.Actions</fmt:message>:</h3> 
    <ul id="contextMenuList">
        <c:forEach var="item" items="${ctxMenu.items}" varStatus="status">
            <%-- multiple item --%>
            <c:if test="${item.supportMultiple}">
	            <li><span class="ui-icon ui-icon-triangle-1-e  " >item</span>
	                <a title='<view:msg>${item.key}</view:msg>' href="javascript:${item.jsFunction}(${item.jsArgs});"><view:msg>${item.key}</view:msg></a>
	            </li>
            </c:if>
            <%-- no multiple item --%>
            <c:if test="${!item.supportMultiple}">
                <li class="no-multiple"><span class="ui-icon ui-icon-triangle-1-e  " >item</span>
                    <a title='<view:msg>${item.key}</view:msg>' href="javascript:${item.jsFunction}(${item.jsArgs});"><view:msg>${item.key}</view:msg></a>
                </li>
            </c:if>

            
        </c:forEach>
    </ul>
</div>
<scrd:loggedusers>
    <div id="reindex" style="display:none;">
        <div class="allowed"></div>
        <div style="padding-bottom: 5px; margin-bottom: 5px;">
            <div>
                <input type="checkbox" id="reindex_check_integrity" checked="checked" /><label for="reindex_check_integrity"> <fmt:message bundle="${lctx}">administrator.menu.dialogs.check_integrity</fmt:message></label>
            </div>
            <div>
                <input type="checkbox" id="reindex_only_newer" /><label for="reindex_only_newer"> <fmt:message bundle="${lctx}">administrator.menu.dialogs.only_newer</fmt:message></label>
            </div>
        </div>
        
    </div>
                <div id="vc_dialog" style="display:none;"><div class="content"></div></div>
</scrd:loggedusers>
<script type="text/javascript">
    
    $(document).ready(function(){
        $('#contextMenuList>li>a').click(function(e) {
            if($(this).parent().hasClass('disabled')){
                e.preventDefault();
            }
        });
        $('#contextMenu>div.scope').click(function(e) {
            var id = $(this).attr('id');
            setScope(id);
        });

        $('#scope_single.viewer').bind('viewReady', function(event, id){
            var t = '<li id="cms_'+ k4Settings.activeUuid+'"><span class="ui-icon ui-icon-triangle-1-e " >item</span>'+$(jq(k4Settings.activeUuid)+">a>label").html()+'</li>';
            $('#context_items_active').html(t);
        });

    });
    
    function clearContextMenuSelection(){
        $('#context_items_selection').html("");
    }
    
    function removeFromContextMenuSelection(id){
        $(jq('cm_'+id)).remove();
    }
    
    function addToContextMenuSelection(id, label){
        var t = '<li id="cm_' + id + '">';
            t += '<span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>';
            t += '<label>'+label+'</label></li>';
            
        $('#context_items_selection').append(t);
    }
    
    function onShowContextMenu(){
        if($('#context_items_selection>li').length>0){
            setScope('scope_multiple');
            $('#scope_multiple').show();
            $('#scope_single').hide();
        }else{
            setScope('scope_single');
            $('#scope_multiple').hide();
            $('#scope_single').show();
        }
    }
    
    function getLabel(pid){
        return $(jq("cm_" + uuids[i])+">label").html();
    }

    function getSinglePid(){
        return $('#context_items_active>li').attr("id").substring(4); //id=cms_model-model_uuid:xxx
    }

    /*
     * returns 'single' or 'multiple'
     */
    function getScope(){
        return $('#contextMenu>div.selected').attr('id').split('_')[1];
    }
    
    function getMultipleSelection(){
        var pids = [];
        $('#context_items_selection>li').each(function(){
            var id = $(this).attr("id").substring(3); //id=cm_model-model_uuid:xxx
            pids.push(id);
        });
        return pids;
    }
    
    /*
     * returns array of affected pids by multiple or single selection
     */
    function getAffectedPids(){
        if(getScope()=='single'){
            return [getSinglePid()];
        }else{
            return getMultipleSelection();
        }
    }
    
    function setScope(id){
        var scope = id.split('_')[1];
        var items = $(jq(id)+">ul>li").length;
        if(scope=="multiple" && items >1){
            $('#contextMenuList>li.no-multiple').addClass('disabled');
        }else{
            $('#contextMenuList>li.no-multiple').removeClass('disabled');
        }
        $('#contextMenu>div.scope').removeClass('selected');
        $(jq(id)).addClass('selected');
    }

    
    
    var _metadataDialog;
    function viewMetadata(){
        var fullpid = getAffectedPids()[0];
        var pid = fullpid.split('_')[1];
        var models = fullpid.split('_')[0].split('-');
        var model = models[models.length-1];

        var titul = $(jq(fullpid)+">a").html();
        if(_metadataDialog){
            _metadataDialog.dialog('open');
        }else{
            $(document.body).append('<div id="metaDataDialog"><div id="metaData"></div></div>')
            _metadataDialog = $('#metaDataDialog').dialog({
                width:640,
                height:480,
                modal:true,
                title:titul,
                buttons: [
                    {
                        text: dictionary['common.close'],
                        click: function() {
                            $(this).dialog("close"); 
                        }
                    }
                ]
            });
        }

        $('#metaData').html('<p align="center"><img src="img/loading.gif" alt="loading" /></p>');
        var url = "inc/details/metadataFull.jsp?pid="+pid+"&model="+model;
        $.get(url, function(data){
            $('#metaData').html(data);
            $('#mods-full').tabs();
        });
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
            '</div>');

            _persistentURLDialog = $('#persistentURL').dialog({
                width:640,
                modal:true,
                title:dictionary["administrator.menu.dialogs.persistenturl.title"],
                buttons:  [
                    {
                        text: dictionary['common.close'],
                        click: function() {
                            $(this).dialog("close"); 
                        }
                    }
                ]
            });
        }
        
        var uuids = getAffectedPids();
        var input;
        $('#'+textFieldID).html('');
        for(var i=0; i<uuids.length; i++){
            input = $(jq("cm_" + uuids[i])+">label").html() + ': <input name="'+textFieldID+'"  style="width:100%;" type="text" '+
                ' id="'+textFieldID+'" value="'+currentURL+"handle/"+uuids[i].split("_")[1]+'"  />';
            $('#'+textFieldID).append(input);
        }
        $('#'+textFieldID+'>input').focus(function() {
            $(this).select();
        });
        
    }
    
    /** Generating pdf */
    function generatepdf() {
        var pids = getAffectedPids();
        var structs = map(function(pid) { 
            var divided = pid.split("_");            
            var structure = {
                       models:divided[0],
                       pid:divided[1]
                };
            return structure;            
            
        }, pids);    
        // show pdf dialog 
        pdf.generate(structs);
    }


    
    function downloadOriginalItem(){
          var pids = getAffectedPids();
          var structs = map(function(pid) { 
              var divided = pid.split("_");            
              var structure = {
                         models:divided[0],
                         pid:divided[1]
                };
              return structure;            
              
          }, pids); 
          // show download original dialog
          downloadOriginal.download(structs);          
    }

  <scrd:loggedusers>
  
  /* Administrate virtual collections */
  var _vcollDialog;
  function vcAddToVirtualCollection(){
      $("#vc_dialog>div.content").html('<p align="center"><img src="img/loading.gif" alt="loading" /></p>');
      if (_vcollDialog) {
            _vcollDialog.dialog('open');
        } else {
            _vcollDialog = $("#vc_dialog").dialog({
                bgiframe: true,
                width: 500,
                modal: true,
                title:'<fmt:message bundle="${lctx}">administrator.menu.dialogs.virtualcollections</fmt:message>',
                buttons: [
                    {
                        text: "Ok",
                        click: function() {
                            vcDoAdd();
                            $(this).dialog("close"); 
                        }
                    },
                    {
                        text: dictionary['common.close'],
                        click: function() {
                            $(this).dialog("close"); 
                        }
                    }
                ]
            });

        }
        $.get("inc/details/vc.jsp", function(data){
            $("#vc_dialog>div.content").html(data);
            var pids = getAffectedPids();
            if(pids.length==1){
                var pidpath = getPidPath(pids[0]);
                var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                $(".vcoll").each(function(){
                    var id = $(this).attr('id');
                    var coll = id.split('vc_')[1];
                    var url = 'vc?action=CHECK&pid=' + pid + "&collection=" + coll;
                    $.get(url, function(data){
                        if(data.toString()=='1'){
                            $(jq(id)+'>td>input').attr("checked", true).addClass('checked');
                        }else{
                            $(jq(id)+'>td>input').attr("checked", false);
                        }
                    });
                });
            }
        });
  }
  
  function vcDoAdd(){
    var pids = getAffectedPids();
    
    var urlbuffer;
    var action;
    var coll;
    var hasChanges = false;
    urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={virtualcollections;";
    if(pids.length==1){
        var pidpath = getPidPath(pids[0]);
        var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
        var j = 0;
        var changed = false;
        $(".vcoll").each(function(){
            var id = $(this).attr('id');
            coll = id.split('vc_')[1];
            if($(jq(id)+'>td>input').is(":checked") && !$(jq(id)+'>td>input').hasClass("checked")){
                action = "add";
                changed = true;
            }else if(!$(jq(id)+'>td>input').is(":checked") && $(jq(id)+'>td>input').hasClass("checked")){
                action = "remove";
                changed = true;
            }else{
                changed = false;
            }
            if(changed){
                hasChanges = true;
                if (j>0) {
                   urlbuffer=urlbuffer + ";";
                }
                j++;
                urlbuffer=urlbuffer+"{"+action+";"+replaceAll(pid, ":","\\:")+";"+replaceAll(coll, ":","\\:")+"}";
            }
        });
        urlbuffer=urlbuffer+"}";
    }else{
        var j = 0;
        hasChanges = true;
        for(var i=0; i<pids.length; i++){
            var pidpath = getPidPath(pids[i]);
            var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
            $(".vcoll").each(function(){
                var id = $(this).attr('id');
                coll = id.split('vc_')[1];
                if($(jq(id)+'>td>input').is(":checked")){
                    action = "add";
                }else{
                    action = "remove";
                }
                if (j>0) {
                   urlbuffer=urlbuffer + ";";
                }
                j++;
                urlbuffer=urlbuffer+"{"+action+";"+replaceAll(pid, ":","\\:")+";"+replaceAll(coll, ":","\\:")+"}";
            });
        }
        urlbuffer=urlbuffer+"}";
                
    }

    
    //alert(urlbuffer);
    if(hasChanges){
        processStarter("virtualcollections").start(urlbuffer);
    }
    
  }

  function ctxPrint(){
      var pids = getAffectedPids();
      var structs = map(function(pid) { 
          var divided = pid.split("_");            
          var structure = {
                     models:divided[0],
                     pid:divided[1]
              };
          return structure;            
          
      }, pids); 
      // show print dialog
      print.print(structs);          
   }
  
  
    var _reindexDialog;
    function reindex(){
        if (_reindexDialog) {
            _reindexDialog.dialog('open');
        } else {
            _reindexDialog = $("#reindex").dialog({
                bgiframe: true,
                width: 500,
                modal: true,
                title:'<fmt:message bundle="${lctx}">administrator.menu.reindex</fmt:message>',
                buttons: [
                    {
                        text: "Ok",
                        click: function() {
                            askReindex();
                            $(this).dialog("close"); 
                        }
                    },
                    {
                        text: dictionary['common.close'],
                        click: function() {
                            $(this).dialog("close"); 
                        }
                    }
                ]
            });

        }
        var pids = getAffectedPids();
        $("#reindex>div.allowed").html($("#context_items_selection").html());
        if(pids.length>1){
            for(var i=0; i<pids.length; i++){
                var pidpath = getPidPath(pids[i]);
                pids[i] = pidpath.substring(pidpath.lastIndexOf("/") + 1);
            }
        }
    }
    
    function askReindex(){
        showConfirmDialog(dictionary['administrator.dialogs.reindexconfirm'], function(){
            doReindex();
        });
    }
    function doReindex(){
        
            var pids = getAffectedPids();
            var action;
            if($("#reindex_only_newer").is(':checked')){
                action = "reindexDoc";
            }else{
                action = "fromKrameriusModel";
            }
            
            var urlbuffer;
            if(pids.length==1){
                var pidpath = getPidPath(pids[0]);
                var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                var title = $(jq(pids[0])+">a>label").text();
                var escapedTitle = replaceAll(title, ',', '');
                escapedTitle = replaceAll(escapedTitle, '\n', '');
                escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
                urlbuffer = "lr?action=start&def=reindex&out=text&params="+action+","+pid+","+escapedTitle;
            }else{
                urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={reindex;"
                for(var i=0; i<pids.length; i++){
                        var pidpath = getPidPath(pids[i]);
                        var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                        var title = $(jq(pids[i])+">a>label").text();
                        var escapedTitle = replaceAll(title, ',', '');
                        escapedTitle = replaceAll(escapedTitle, '\n', '');
                        escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
                        urlbuffer=urlbuffer+"{"+action+";"+replaceAll(pid, ":","\\:")+";"+replaceAll(escapedTitle, ":","\\:")+"}";
                        if (i<pids.length-1) {
                           urlbuffer=urlbuffer+";" 
                        }
                }
                urlbuffer=urlbuffer+"}";
            }

            processStarter("reindex").start(urlbuffer);
    }
        
    function deletefromindex(){

        showConfirmDialog(dictionary['administrator.dialogs.deleteconfirm'], function(){
            var pids = getAffectedPids();

            var action = "deleteDocument";
            
            var urlbuffer;
            if(pids.length==1){
                var pidpath = getPidPath(pids[0]);
                var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                var title = $(jq(pids[0])+">a>label").text();
                var escapedTitle = replaceAll(title, ',', '');
                escapedTitle = replaceAll(escapedTitle, '\n', '');
                escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
                urlbuffer = "lr?action=start&def=reindex&out=text&params="+action+","+pid+","+escapedTitle;
            }else{
                urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={reindex;"
                for(var i=0; i<pids.length; i++){
                        var pidpath = getPidPath(pids[i]);
                        var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                        var title = $(jq(pids[i])+">a>label").text();
                        var escapedTitle = replaceAll(title, ',', '');
                        escapedTitle = replaceAll(escapedTitle, '\n', '');
                        escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
                        urlbuffer=urlbuffer+"{"+action+";"+replaceAll(pid, ":","\\:")+";"+replaceAll(escapedTitle, ":","\\:")+"}";
                        if (i<pids.length-1) {
                           urlbuffer=urlbuffer+";" 
                        }
                }
                urlbuffer=urlbuffer+"}";
            }
                
            processStarter("reindex").start(urlbuffer);
        });
        
    }
        
    function deletePid(){
        var pids = getAffectedPids();
        
        showConfirmDialog(dictionary['administrator.dialogs.deleteconfirm'], function(){
            var urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={delete;"
            for(var i=0; i<pids.length; i++){
                    var pidpath = getPidPath(pids[i]);
                    var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                    urlbuffer=urlbuffer+"{"+replaceAll(pid, ":","\\:")+";"+replaceAll(pidpath, ":","\\:")+"}";
                    if (i<pids.length-1) {
                       urlbuffer=urlbuffer+";" 
                    }
            }

            processStarter("delete").start(urlbuffer);

        });
    }
        
    function exportFOXML(){
        var structs = pidstructs();     
        if (structs.length > 1) {
            var u = urlWithPids("lr?action=start&def=aggregate&out=text&nparams={export;",structs)+"}";
            processStarter("export").start(u);
        } else {
            var u = urlWithPids("lr?action=start&def=export&out=text&nparams=",structs);
            processStarter("export").start(u);
        }
    }

    function exportToCD(img, i18nServlet, country,language) {
        var structs = pidstructs();     
        if (structs.length > 0) {
            var u = "lr?action=start&def=static_export_CD&out=text&nparams={"+structs[0].pid.replaceAll(":","\\:")+";"+img+";"+i18nServlet+";"+country+";"+language+"}";
            processStarter("static_export_CD").start(u);
        }
    }
    function exportToDVD(img, i18nServlet, country,language) {
        var structs = pidstructs();     
        if (structs.length > 0) {
            var u = "lr?action=start&def=static_export_CD&out=text&nparams={"+structs[0].pid.replaceAll(":","\\:")+";"+img+";"+i18nServlet+";"+country+";"+language+"}";
            processStarter("static_export_DVD").start(u);
        }
    }
    
        
    function generateDeepZoomTiles(){
        var structs = pidstructs();     
        var u = urlWithPids("lr?action=start&def=aggregate&out=text&nparams={generateDeepZoomTiles;",structs);
        processStarter("generateDeepZoomTiles").start(u);
    }
        
    function deleteGeneratedDeepZoomTiles(){
        var pids = getAffectedPids();
        var structs = pidstructs();
        var u = urlWithPids("lr?action=start&def=aggregate&out=text&nparams={deleteGeneratedDeepZoomTiles;",structs);
        processStarter("deleteGeneratedDeepZoomTiles").start(u);
    }
        
    function securedActionsTableForCtxMenu(read, administrate){
        var pids = getAffectedPids();
        var structs = pidstructs();
        
        
        // open dialog
        findObjectsDialog().openDialog(structs);
    }

    function securedStreamsTableForCtxMenu(read, administrate){
        var pids = getAffectedPids();
        var structs = pidstructs();
        
        if (!selectStreams) {
        	selectStreams = new SecuredStreamsSelection();   
        }

        // open dialog
        selectStreams.openDialog(structs);
    }
    
    
    function openEditor(url){
        window.open(url,'_blank');   
    }
        
</scrd:loggedusers>
    
</script>