<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<view:object name="adminMenuViewObject" clz="cz.incad.Kramerius.views.adminmenu.AdminMenuViewObject"></view:object>

<style type="text/css">
    #adminMenu{
        padding:5px;
        display: none;
        position:absolute;
        right:0;
        z-index:100;
        width:380px;
        border: 1px solid gray;
    }
    
    #adminMenuItems{
        padding:5px;
    }
    #adminMenuItems>div{
        padding:2px;
    }
    
    #adminMenuItems > span {
    display: block;
    float: left;
    height: 16px;
    overflow: hidden;
    text-indent: -99999px;
    width: 16px;
}

    #adminMenu>div.header{
        height:20px;
        text-align: center;
        width:100%;
        font-weight: bold;
    }
    #adminMenu>div.footer{
        text-align: right;
        width:100%;
        height:20px;
    }
     
         
</style>
<div id="adminMenu" class="shadow ui-widget-content">
    <div class="header"><fmt:message bundle="${lctx}">administrator.menu</fmt:message></div>
    <div id="adminMenuItems" class="adminMenuItems">
        <c:forEach var="item" items="${adminMenuViewObject.adminMenuItems}">
            <span class="ui-icon ui-icon-triangle-1-e  ">item</span>
            ${item}
        </c:forEach>
    </div>
    <div class="footer">
        <input type="button" value="close" class="ui-state-default ui-corner-all"  onclick="hideAdminMenu();" />
    </div>
</div>
    
<!-- vypis procesu -->
<div id="processes" style="display:none;"></div>

<!-- confirmation dialog -->
<div id="confirm_dialog" title="Potvrdit" style="display:none;">
	<img src="img/alert.png" alt="alert" />

        <span id="proccess_confirm_text"></span>
</div>

<!-- administrace virtualnich sbirek -->
<div id="vcAdminDialog" style="display:none;">
    <div class="content"><fmt:message bundle="${lctx}" key="administrator.dialogs.waiting" /></div>
</div>

<!-- indexace dokumentu -->
<div id="indexer" style="display:none;">
    <div id="indexerContent"><fmt:message bundle="${lctx}" key="administrator.dialogs.waiting" /></div>
</div>

<!-- common -->
<div id="common_started" style="display:none;">
	<div id="common_started_waiting" style="margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center" id="common_started_text"></td></tr>
    	</table>
	</div>
	<div id="common_started_ok" style="margin: 12px;display:none;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_ok"><br/></p>
	</div>
	<div id="common_started_failed" style="margin: 12px;display:none;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_failed"></p>
	</div>
</div>

<script type="text/javascript">
    /* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function(){
    $('body').click(function() {
    	hideAdminMenu();
        //hideContextMenu();
    });
});

function showConfirmDialog(t,f){
    $("#confirm_dialog").dialog('destroy');
    $( "#proccess_confirm_text" ).html(t);
    $( "#confirm_dialog" ).dialog({
        resizable: false,
        height:140,
        modal: true,
        buttons: [{
            text:dictionary['common.ok'],
            click:function() {
                $(this).dialog('destroy');
                f();
            }
        },{
            text:dictionary['common.close'],
            click:function() {
                $(this).dialog('destroy');
            }
        }]
    });
}

function showAdminMenu() {
	$("#adminMenu").css("top",$("#header").offset().top + $("#header").height()+4);
	$("#adminMenu").show();
}

function hideAdminMenu() {
	$("#adminMenu").css("display","none");
}




function enumerator() {
    showConfirmDialog(dictionary['administrator.dialogs.enumerator.confirm'], function(){
        noParamsProcess('enumerator');
    });
}

function replicationRights() {
    showConfirmDialog(dictionary['administrator.dialogs.replicationrights.confirm'], function(){
        noParamsProcess('replicationrights');
    });
 
}


function convert() {
    showConfirmDialog(dictionary['administrator.dialogs.convert.confirm'], function(){
        noParamsProcess('convert');
    });
 
}

function impor() {
    showConfirmDialog(dictionary['administrator.dialogs.import.confirm'], function(){
        noParamsProcess('import');
    });
}

function importMonographs() {
    showConfirmDialog(dictionary['administrator.dialogs.importMonograph.confirm'], function(){
		var url = "lr?action=start&def=replikator_monographs&out=text";
	    processStarter("import").start(url);
    });
}


function importPeriodicals() {
    showConfirmDialog(dictionary['administrator.dialogs.importPeriodical.confirm'], function(){
        var url = "lr?action=start&def=replikator_periodicals&out=text";
        processStarter("import").start(url);
    });
}

function replaceAll(txt, replace, with_this) {
	  return txt.replace(new RegExp(replace, 'g'),with_this);
}

/**
 * Reindexace
 * @param level
 * @param model
 * @return
 */
function reindex(level, model) {
	hideAdminOptions(level);
	var uuid = $("#tabs_"+level).attr('pid');
	var title = $("#tabs_"+level + ">div>div[id=info-"+model+"]").text();
	var escapedTitle = replaceAll(title, ',', '');
	escapedTitle = replaceAll(escapedTitle, '\n', '');
        escapedTitle = escapedTitle.replace(/ +(?= )/g,'');

	var url = "lr?action=start&def=reindex&out=text&params=reindexDoc,"+uuid+","+escapedTitle;

	processStarter("reindex").start(url);
    
}



function exportTOFOXML(level)  {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	var pidpath = COMMON.pidpath(level);
	var url = "lr?action=start&def=export&out=text&params="+pid;

	processStarter("foexport").start(url);
}


function noParamsProcess(process)  {
	var url = "lr?action=start&def="+process+"&out=text";
    processStarter(process).start(url);
}

function deleteUuid(level, model)  {
	hideAdminOptions(level);
 
	showConfirmDialog(dictionary['administrator.dialogs.deleteconfirm'], function(){
		var pid = $("#tabs_"+level).attr('pid');
		var pidpath = COMMON.pidpath(level);
		var url = "lr?action=start&def=delete&out=text&params="+pid+","+pidpath;
	
	   processStarter("delete").start(url);
			
	});
}

/**
 * Generovani staticke exportu
 * @param level
 * @return
 */
function generateStatic(level, exportType, imgUrl, i18nUrl,iso3Country, iso3Lang){
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def="+exportType+"&out=text&params="+pid+","+imgUrl+","+i18nUrl+","+iso3Country+","+iso3Lang;

    processStarter("staticPDF").start(url);
	 
}


var _vcAdminDialog;
function showVirtualCollectionsAdmin(){
    $("#vcAdminDialog>div.content").html('<p align="center"><img src="img/loading.gif" alt="loading" /></p>');
    if (_vcAdminDialog) {
        _vcAdminDialog.dialog('open');
    } else {
    	_vcAdminDialog = $("#vcAdminDialog").dialog({
            bgiframe: true,
            width: 700,
            height: 400,
            modal: true,
	        title: dictionary['administrator.menu.dialogs.virtualcollections.title'],
            buttons: [{
                text:dictionary['common.close'],
                click: function() {
                    $(this).dialog("close") 
                } 
            }]
        });
    }
    $("#vcAdminDialog>div.content").load("inc/admin/_virtual_collection_admin.jsp");
}



var _indexerDialog;
/**
 * Zobrazuje spravu indexace
 */
function showIndexerAdmin(){
    hideAdminMenu();
    var url = "inc/admin/_indexer_data.jsp?offset=0";
    $.get(url, function(data) {
        $("#indexerContent").html(data);
        checkIndexed();
    });
    if (_indexerDialog) {
        _indexerDialog.dialog('open');
    } else {
    	_indexerDialog = $("#indexer").dialog({
            bgiframe: true,
            width: 700,
            height: 400,
            modal: true,
	        title: dictionary['administrator.menu.dialogs.indexDocuments.title'],
            buttons: [{
                text:dictionary['common.close'],
                click: function() {
                    $(this).dialog("close") 
                } 
            }]
        });
    }
}

function checkIndexed(){
    var url;
    var pid;
    $('.indexer_result').each(function(){
        pid = $(this).attr('pid');
        var prefix = "info\:fedora\/";
        pid = pid.replace(prefix,"");
        var obj = this;
        url = "inc/admin/_indexer_check.jsp?pid="+pid;
        $.get(url, function(data) {
            if(trim10(data)=="1"){
              $(obj).children('td:eq(0)').addClass("indexer_result_indexed");
            }else{
               $(obj).children('td:eq(0)').addClass("indexer_result_notindexed"); 
            }
        });
    });
}




function deletefromindex(level){
	hideAdminOptions(level);
    showConfirmDialog('Confirm delete dokument from index', function(){
       var pid = $("#tabs_"+level).attr('pid');
       var pid_path = "";
       for(var i = level; i>0; i--){
           pid_path = $('#tabs_'+i).attr('pid') + pid_path;
           if(i>1) {pid_path = '/' + pid_path};
       }
       var url = "lr?action=start&def=reindex&out=text&params=deleteDocument,"+pid_path+","+pid;
       processStarter("delindex").start(url);
    });
}

function confirmIndexDocByPid(pid){
    var url = "inc/admin/_indexer_get_title.jsp?pid="+pid;
    $.get(url, function(data) {
        showConfirmDialog('Confirm index dokumentu: ' + data, function(){
          //var prefix = "info\:fedora\/uuid:";
          //var uuid = pid.replace(prefix,"");
          var escapedTitle = replaceAll(data, ',', '');
          var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+pid+","+escapedTitle;
          processStarter("reindex").start(url);
        });        
    }).error(function(){
        alert("PID not found");
    });
}

function confirmIndexModel(model){
    showConfirmDialog('Confirm index model: ' + model, function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      processStarter("reindex").start(url);
    });
}

function checkIndexIntegrity(){
var text = dictionary['administrator.dialogs.confirm'] + " " + dictionary['administrator.menu.dialogs.check_integrity'];
    showConfirmDialog(text, function(){
      var url = "lr?action=start&def=reindex&out=text&params=checkIntegrity,check,Check integrity";
      processStarter("reindex").start(url);
    });
}

function indexDoc(pid, title){
    showConfirmDialog('Confirm index dokumentu', function(){
    var prefix = "info\:fedora\/";
    var pid2 = pid.replace(prefix,"");
      var escapedTitle = replaceAll(title, ',', '');
      var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+pid2+","+escapedTitle;
      processStarter("reindex").start(url);
    });
}

function indexModel(model){
    showConfirmDialog('Confirm index cely model', function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      processStarter("reindex").start(url);
    });
}

function getAllowed(action, pids, div){
    var s = "<ul>";
    var url = "isActionAllowed?action="+action;
    for(var i=0; i<pids.length; i++){
        url += "&pid=" + pids[i];
    }
    $.getJSON(url, function(data) {
        $.each(data, function(key, val) {
            s += '<li id="' + key + '">' + val + '</li>';
        });
        s += '</ul>';
        $(div).html(s);
    });
}


function ShowSearchHistory() {
    this.dialog = null;     
}


ShowSearchHistory.prototype.showHistory = function() {
    $.get("profile?action=GET", bind(function(data){
        
        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            var pdiv = '<div id="searchHistory"></div>';
            $(document.body).append(pdiv);
            this.dialog = $("#searchHistory").dialog({
                bgiframe: true,
                width:  800,
                height:  400,
                modal: true,
                title: '',
                buttons: 
                    [{
                        text:dictionary['common.close'],
                        click:function() {
                            $(this).dialog("close") 
                        } 
                    }]
            });

        }

        var htmlheader = "<table style='width:100%'>"+
        "<thead><tr>"+
        "<td><strong>"+dictionary['administrator.menu.dialogs.profile.searchedWords']+"</strong></td>"
        +"<td><strong>"+dictionary['administrator.menu.dialogs.profile.searchedUrl']+"</strong></td>"
        +"<td><strong>"+dictionary['administrator.menu.dialogs.profile.searchedRSS']+"</strong></td>"
        +"</tr> </thead>"+
        "<tbody>";


        function keys(elm) {
            var ret = [];
            for(var key in elm) {
                ret.push(key);
            }
            return ret;
        }

        function field(element, arr, field, key) {
            var f = element[field];
            if (f) {
                return "<strong>"+(key ?  dictionary[key] : dictionary[field]) +"</strong>:"+f; 
            } else return "";
         }
        
        function facet(element,arr) {
        	var fq_index = arr.indexOf("fq");
            if (fq_index >= 0) {
            	var fq = element['fq'];
                var retval = reduce(function(base, fqel, status) {
                    var splitted = fqel.split(':');
                    var type = splitted[0];
                    var val = splitted[1];
                    if (val.startsWith('"') && val.endsWith('"')) {
                        val = val.substring(1,val.length-1);
                    }
                    return base+"<strong>"+dictionary["facet."+type]+"</strong>:"+ (dictionary[val] ? dictionary[val]:val) +(status.last ? "" : " ");                
                },"",fq);
                return retval;
                
            } else return "";
        }

        function casovaosa(element,arr) {
            var da_od_index = arr.indexOf("da_od");
            var da_do_index = arr.indexOf("da_do");
            if (da_od_index >= 0 && da_do_index>=0) {
                var da_od = element["da_od"];
                var da_do = element["da_do"];
                if (da_od_index >= 0) arr.rm(arr.indexOf("da_od")); 
                if (da_do_index >= 0) arr.rm(arr.indexOf("da_do")); 
                return "<strong>"+dictionary['common.date']+"</strong>:"+da_od+"-"+da_do; 
            } else return "";
        }

        function query(element,arr) {
            var q = element["q"];
            if (q) {
                return "<strong>"+dictionary['filter.query']+"</strong>"+q;
            } else return "";
        }

        function append(prev, nval) {
            if (nval.length > 0 && prev.length > 0) {
                prev = prev +", ";
            }
            prev = prev+nval;
            return prev;
        }
        
        var html = data['searchHistory'] ? reduce(function(base, element, status) {
           var k = keys(element);
           var el = casovaosa(element,k);
           el = append(el, query(element,k));
           el = append(el, facet(element,k));
           
           el = append(el,field(element,k,'title'));
           el = append(el,field(element,k,'rok'));
           el = append(el,field(element,k,'issn'))
           el = append(el,field(element,k,'author'));
           el = append(el,field(element,k,'udc'));
           el = append(el,field(element,k,'ddc'));
           el = append(el,field(element,k,'browse_title','suggest.search_title'));
           
        	base = base + "<tr>"+
            "<td>"+ el +"</td>"+
            "<td> <a class='ui-icon ui-icon-link' href='"+ element["url"]+"&fromProfile=true'>_link</a></td>"+
            "<td> <a class='ui-icon ui-icon-signal-diag' href='"+ element["rss"]+"&fromProfile=true'></a></td>"+
            "</tr>";       
            return base;         
        }, htmlheader, data['searchHistory'].reverse())+"</tbody></table>" : htmlheader;
                   
        $("#searchHistory").html(html);
        
    },this));
}

var showSearchHistory = new ShowSearchHistory();



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
        
    },this));
}

var changeFlag = new ChangeFlag();



</script>