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
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration" %>
<%@page import="cz.incad.kramerius.security.SecuredActions" %>

<view:object name="ctxView" clz="cz.incad.Kramerius.views.ContextMenuViewObject"></view:object>

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
    
    #context_items_selection li{
        line-height: 18px;
        
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
    
    #metaData{
        height: calc(100% - 3px);
    }


    #reindex ul{
        margin: 2px;
        padding-left: 4px;
    }
    #reindex li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 18px;
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
        border-bottom:1px solid rgba(0, 30, 60, 0.9);
        margin-bottom:5px;
        padding-bottom:3px;
    }
    #feedbackDialog{
        display:none;
    }
</style>
<div><h3><view:msg>administrator.menu.Scope</view:msg>:</h3></div>
<div class="scope selected viewer" id="scope_single"><span><view:msg>administrator.menu.active</view:msg></span>
    <ul id="context_items_active"></ul>
</div>
<div class="scope" id="scope_multiple"><span><view:msg>administrator.menu.selected.scope</view:msg></span>
    <ul id="context_items_selection"></ul>
</div>




<div style="height:0px;border-top:1px solid silver;"></div>
<div><h3><view:msg>administrator.menu.Actions</view:msg>:</h3>
    <ul id="contextMenuList">
         <c:forEach var="part" items="${ctxView.contextMenu.parts}" varStatus="status">


           <c:if test="${part.renderable}">
               <c:if test="${status.index > 0}">
                     <hr/>
                 </c:if>
               <c:forEach var="item" items="${part.items}" >
                   <c:if test="${item.renderable}">${item.renderedItem}</c:if>
               </c:forEach>
             </c:if>
         </c:forEach>
    </ul>
</div>

<div id="feedbackDialog">
    <h3><view:msg>administrator.menu.feedback.title</view:msg>:</h3>
    <table>
        <tr>
            <td valign="top"><view:msg>administrator.menu.feedback.from</view:msg></td>
            <td><input type="text" id="feedback_from" /></td>
        </tr>
        <tr>
            <td valign="top"><view:msg>administrator.menu.feedback.content</view:msg></td>
            <td><textarea id="feedback_content" cols="60" rows="10" ></textarea></td>
        </tr>
    </table>
</div>

<scrd:loggedusers>
    <div id="reindex" style="display:none;">
        <div class="allowed"></div>
        <div style="padding-bottom: 5px; margin-bottom: 5px;">
            <div>
                <input type="checkbox" id="reindex_check_integrity" checked="checked" /><label for="reindex_check_integrity"> <view:msg>administrator.menu.dialogs.check_integrity</view:msg></label>
                </div>
                <div>
                    <input type="checkbox" id="reindex_only_newer" /><label for="reindex_only_newer"> <view:msg>administrator.menu.dialogs.only_newer</view:msg></label>
                </div>
            </div>

        </div>
        <div id="vc_dialog" style="display:none;"><div class="content"></div></div>
</scrd:loggedusers>

<script  src="js/localprint/localprint.js" language="javascript" type="text/javascript"></script>
<script  src="js/underscore-min.js" language="javascript" type="text/javascript"></script>
<link href="js/prettify.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="js/prettify.js"></script>
<script src="js/mods.js" type="text/javascript" ></script>
<script type="text/javascript">
    
    var policyPublic = ${policyPublic};

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
            if(!initView){
                var t = '<li id="cms_'+ k4Settings.activeUuid+'"><span class="ui-icon ui-icon-triangle-1-e " >item</span>'+$(jq(k4Settings.activeUuid)+">div>a>label").html()+'</li>';
                $('#context_items_active').html(t);
            }
        });

    });


    function setInitActive(){
        var initialPid = model_path_str.replaceAll('/', '-') + "_" + pid_path[pid_path.length - 1];
        $('#context_items_active').html('<li id="cms_'+ initialPid+'"><span class="ui-icon ui-icon-triangle-1-e " >item</span><label>'+$(jq(initialPid)+">div>a>label").html()+'</label></li>');
    }

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
        
        if ($('#context_items_active>li').size() > 0) {
            _checkMenuItems();
        }
    }
    
    function _checkMenuItems() {
        var pids = getAffectedPids();
        pids = map(function(pid) { 
            var divided = pid.split("_");            
            return divided[1];
        }, pids);
        
        var actions = [];
        $("#contextMenuList>li[data-action]").each(function(i, v) { 
            var act = $(v).data('action');
            if ($.inArray( act, actions ) < 0) {
               actions.push(act);
            }
        });

        var pidString = reduce(function(base, element, status) {
            if (!status.first) {
               base = base + "&";
            }
            return base + "pid="+element.trim();
         }, "", pids);

         var actsString = reduce(function(base, element, status) {
             if (!status.first) {
                base = base + "&";
             }
             return base + "actions="+element.trim();
          }, "", actions);
         
         $.each(actions, function(i,v) {
             $.get("isActionAllowed?action="+v+"&"+pidString,bind(function(data) {
                 var flag = true;
                 for(var pid in data) {
                   var f = data[pid];
                   flag = f & flag;
                 }
                 var elms = $("#contextMenuList>li[data-action]");
                 elms.each(function(k,p) {
                     if ($(p).data('action') === v) {
                         if (flag) {
                             $(p).css('display','block');
                         } else {
                             $(p).css('display','none');
                         }
                     }
                 });
             },this));
         });
    }
    
    function isPrivate(id){
    	return $(jq(id)).hasClass('private');
    }
    
    function isPublic(id) {
    	return $(jq(id)).hasClass('public');
    }

    var _feedbackDialog;
    function feedbackDialog(){
        if(_feedbackDialog){
            _feedbackDialog.dialog('open');
        }else{
            _feedbackDialog = $('#feedbackDialog').dialog({
                width:500,
                height:330,
                modal:true,
                title: '<view:msg>administrator.menu.feedback.title</view:msg>',
                buttons: [
                    {
                        text: dictionary['common.send'],
                        click: function() {
                            sendFeedback();
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
    }

    function sendFeedback(){
        var pid = "";
        var urls = getPersistentURLs();
        for(var i=0; i<urls.length; i++){
            pid = pid + '[' + urls[i] + '] ';
        }

        var url = "feedback?from="+$("#feedback_from").val() +
            "&pid="+pid +
            "&content="+encodeURIComponent($("#feedback_content").val());
        $("#feedback_sending").show();
        $.post(url, function(data){
            alert('<view:msg>administrator.menu.feedback.success</view:msg>');
        }).error(function(data, msg, status){
            alert('<view:msg>administrator.menu.feedback.fail</view:msg>');
            $("#feedback_sending").hide();
        });
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
            
            $('#mods-xml>pre').addClass('lang-html');
            $('#mods-xml>pre').addClass('prettyprint');
            $('#mods-xml>pre').css('border', 'none');
            prettyPrint();
            
            /*
            var modsid = "mods_"+pid;
            var e = $('<div class="modsxml"></div>');
            //elem.append(e);

            var div = $('<div style="display:block;" />');
            div.attr("id", modsid);

            div.append(e);
            
            var xmlData = $('#mods-xml>pre').text();

            var modsXml = new ModsXml(e);
            modsXml.loadXmlFromString(xmlData, e, function(){});
            modsXml.renderTree();
            modsXml.expand();
            $('#mods-xml').append(div);
            */
        });
    }



    var _persistentURLDialog;
    function persistentURL(){
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

        var uuids = getAffectedPids();
        var input;
        $('#'+textFieldID).html('');
        var prefix;
        if(getScope()=='single'){
            prefix = 'cms_';
        }else{
            prefix = 'cm_';
        }
        var urls = getPersistentURLs();
        for(var i=0; i<urls.length; i++){
            input = $(jq(prefix + uuids[i])+">label").html() + ': <input style="width:100%;" type="text" '+
                'value="'+urls[i]+'"  />';
            $('#'+textFieldID).append(input);
        }
        $('#'+textFieldID+'>input').focus(function() {
            $(this).select();
        });

    }


    function getPersistentURLs(){
        var urls = new Array();

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


        var uuids = getAffectedPids();
        for(var i=0; i<uuids.length; i++){
            urls.push(currentURL+"handle/"+uuids[i].split("_")[1]);
        }
        return urls;

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


      /* add to favorite */
      function addToFavorites() {
		  if (getAffectedPids().length == 0)   {
              (new Message("favorites_no_selection")).show();
			  return;
		  }

          new Profile().modify(
                  function(json){
                      if (!json.favorites) {
                          json["favorites"] = [];
                      }
                      var structs = map(function(pid) {
                          var divided = pid.split("_");
                          var structure = {
                              models:divided[0],
                              pid:divided[1]
                          };
                          return structure;

                      },getAffectedPids());

                      reduce(function(base, element, status){
                          if (base.indexOf(element.pid) < 0) {
                              base.push(element.pid);
                          }
                          return base;
                      },json["favorites"],structs);

                      return json;
                  }, function () {
                      (new Message("favorites_add_success")).show();
                  });
      }

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
                  title:dictionary['administrator.menu.dialogs.virtualcollections.title'],
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
          
          
          //$("#reindex>div.allowed").html($("#context_items_selection").html());
          var t = "";
          for(var i=0; i<pids.length; i++){
            var id = pids[i];
            //var label = $(jq(id)+">div>a>label").html();
            var label = $(jq("cm_" + id)+">label").html();
            t += '<li>';
            t += '<span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>';
            t += '<label>'+label+'</label></li>';

          }
          $("#reindex>div.allowed").html(t);
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
              //var title = $(jq(pids[0])+">div>a>label").text();
              var title = $(jq("cm_" + pids[0])+">label").text();
              var escapedTitle = replaceAll(title, ',', '');
              escapedTitle = replaceAll(escapedTitle, '\n', '');
              escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
              urlbuffer = "lr?action=start&def=reindex&out=text&params="+action+","+pid+","+escapedTitle;
          }else{
              urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={reindex;"
              for(var i=0; i<pids.length; i++){
                  var pidpath = getPidPath(pids[i]);
                  var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                  //var title = $(jq(pids[i])+">div>a>label").text();
                  var title = $(jq("cm_" + pids[i])+">label").text();
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

          showConfirmDialog(dictionary['administrator.dialogs.indexdeleteconfirm'], function(){
              var pids = getAffectedPids();

              var action = "deleteDocument";

              var urlbuffer;
              if(pids.length==1){
                  var pidpath = getPidPath(pids[0]);
                  var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                  var title = $(jq(pids[0])+">div>a>label").text();
                  var escapedTitle = replaceAll(title, ',', '');
                  escapedTitle = replaceAll(escapedTitle, '\n', '');
                  escapedTitle = escapedTitle.replace(/ +(?= )/g,'');
                  urlbuffer = "lr?action=start&def=reindex&out=text&params="+action+","+pid+","+escapedTitle;
              }else{
                  urlbuffer = "lr?action=start&def=aggregate&out=text&nparams={reindex;"
                  for(var i=0; i<pids.length; i++){
                      var pidpath = getPidPath(pids[i]);
                      var pid = pidpath.substring(pidpath.lastIndexOf("/") + 1);
                      var title = $(jq(pids[i])+">div>a>label").text();
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

      function serverSort() {
          var structs = pidstructs();
          var u = "lr?action=start&def=sort&out=text&nparams={"+structs[0].pid.replaceAll(":","\\:")+"}";
          processStarter("sort").start(u);
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


      function applyMovingWall(){
          var structs = pidstructs();
          if (structs.length > 1) {
              var u = urlWithPids("lr?action=start&def=aggregate&out=text&nparams={applymw;",structs)+"}";
              processStarter("applymw").start(u);
          } else {
              var u = urlWithPids("lr?action=start&def=applymw&out=text&nparams=",structs);
              processStarter("applymw").start(u);
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
          var affectedDialog = findObjectsDialog();
          affectedDialog.actions = null;
          affectedDialog.openDialog(structs);
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
          var pids = getAffectedPids();
          var structs = pidstructs();
          if(structs.length>=1){
              window.open(url+"?pids="+structs[0].pid,'_blank');
          }
      }



      /** change policy flag  */
      function ChangeFlag() {
          this.dialog = null;
          this.policyName = "setpublic";
          this.aggregate = true;
      }

      ChangeFlag.prototype.startProcess = function() {

          
          function _url(/** String */baseUrl, /** Array */ pids) {
              return baseUrl+""+reduce(function(base, item, status) {
                  
                  base = base+"{"+item.pid.replaceAll(":","\\:")+ (status.last ? "}": "};");
                  return base;
              }, "",pids)+"";        
          }

          var value = $("#changeFlag input:checked").val();
          this.policyName = value;
          var structs = pidstructs();     
          this.aggregate = structs.length > 1;
          var u = this.aggregate ?  _url("lr?action=start&out=text&def=aggregate&out=text&nparams={"+this.policyName+";",structs)+"}" : "lr?action=start&out=text&def="+this.policyName+"&nparams={"+structs[0].pid.replaceAll(":","\\:")+"}";
          
          processStarter(this.policyName).start(u);
      }

      ChangeFlag.prototype.containsPrivateAttribute = function(modelPidIdent) {
          var al = $(jq(modelPidIdent)+" span[@title='private']");
          alert(al);
      }
          
      ChangeFlag.prototype.change = function() {

    	  $.get("inc/admin/_change_flag.jsp", bind(function(data){

              
              if (this.dialog) {
                  this.dialog.dialog('open');
              } else {
                  var pdiv = '<div id="changeflagDialog"></div>';

                  $(document.body).append(pdiv);

                  this.dialog = $("#changeflagDialog").dialog({
                      bgiframe: true,
                      width:  400,
                      height:  200,
                      modal: true,
                      title: dictionary['administrator.menu.dialogs.changevisflag.title'],
                      buttons: 
                          [{
                              text:dictionary['common.apply'],
                              click:bind(function() {
                                  this.dialog.dialog("close");
                                  this.startProcess();                        
                               },this)
                          },{
                              text:dictionary['common.close'],
                              click:function() {
                                  $(this).dialog("close") 
                              } 
                          }]
                  });
                      
              }
              $("#changeflagDialog").html(data);

              var html = reduce(function(base,element, status) {
                  //TODO: isPrivate nefunguje 
            	  //function containsPrivateAttribute(modelPidIdent) {
                  //    return $(jq(modelPidIdent)+" >div>a>span[title=\"public\"]").size() > 0;
                  //}
                  
                  var pub = isPublic(element);
                  var priv = isPrivate(element);
                  
                  var key ="";
                  if (pub && !priv) {
					key = "administrator.dialogs.changevisibility.public";
                  } else if (!pub && priv) {
					key = "administrator.dialogs.changevisibility.private";
                  } else {
					key = "administrator.dialogs.changevisibility.uknown";
                  }

                  var label = $(jq(element)+">div>a>label").text();
                  return base+  "<tr><td><span class='ui-icon ui-icon-triangle-1-e folder'>folder</span></td> <td width='60%'>"+label+"</td> <td><strong>"+dictionary[key]+"</strong></td>  </tr>";                 
               },"<table style='width:100%;'>",getAffectedPids())+"</table>";
              
              $("#changeFlag_pids").html(html);

          },this));
      }

      var changeFlag = new ChangeFlag();
            
    </scrd:loggedusers>

</script>