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
        background:white;
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
        buttons: {
            Ok: function() {
                $(this).dialog('destroy');
                f();
                //return true;
            },
            Cancel: function() {
                $(this).dialog('destroy');
                //return false;
            }
        }

    });
}

function showAdminMenu() {
	$("#adminMenu").css("top",$("#header").offset().top + $("#header").height()+4);
	$("#adminMenu").show();
}

function hideAdminMenu() {
	$("#adminMenu").css("display","none");
}


/**
 * Promenne ve scriptu
 */
// Command pattern
var _texts=function() {
	var intArr = new Array(); {
		intArr["[static_export_CD]WAITING"]='administrator.dialogs.waitingstaticPDF';
		intArr["[static_export_CD]PLANNED"]='administrator.dialogs.staticPDFrunning';
		intArr["[static_export_CD]FAILED"]='administrator.dialogs.staticPDFfailed';

		intArr["[reindex]WAITING"]='administrator.dialogs.waitingreindex';
		intArr["[reindex]PLANNED"]='administrator.dialogs.reindexrunning';
		intArr["[reindex]FAILED"]='administrator.dialogs.reindexfailed';

		
		intArr["[replikator_monographs]WAITING"]='administrator.dialogs.waitingmonographimport';
		intArr["[replikator_monographs]PLANNED"]='administrator.dialogs.monographimportrunning';
		intArr["[replikator_monographs]FAILED"]='administrator.dialogs.monographimportfailed';
		
		intArr["[replikator_periodicals]WAITING"]='administrator.dialogs.waitingperiodicsimport';
		intArr["[replikator_periodicals]PLANNED"]='administrator.dialogs.periodicsimportrunning';
		intArr["[replikator_periodicals]FAILED"]='administrator.dialogs.periodicsimportfailed';
		
		intArr["[enumerator]WAITING"]='administrator.dialogs.waitingenumerator';
		intArr["[enumerator]PLANNED"]='administrator.dialogs.enumeratorrunning';
		intArr["[enumerator]FAILED"]='administrator.dialogs.enumeratorfailed';
				
		intArr["[replicationrights]WAITING"]="administrator.dialogs.waitingreplicationrights";
		intArr["[replicationrights]PLANNED"]="administrator.dialogs.replicationrightsrunning";
		intArr["[replicationrights]FAILED"]="administrator.dialogs.replicationrightsfailed";

		intArr["[delete]WAITING"]="administrator.dialogs.waitingdelete";
		intArr["[delete]PLANNED"]="administrator.dialogs.deleterunning";
		intArr["[delete]FAILED"]="administrator.dialogs.deletefailed";


		intArr["[replicationrights]WAITING"]="administrator.dialogs.waitingreplicationrights";
		intArr["[replicationrights]PLANNED"]="administrator.dialogs.replicationrightsrunning";
		intArr["[replicationrights]FAILED"]="administrator.dialogs.replicationrightsfailed";

		intArr["[setpublic]WAITING"]="administrator.dialogs.waitingchangevisflag";
		intArr["[setpublic]PLANNED"]="administrator.dialogs.setprivaterunning";
		intArr["[setpublic]FAILED"]="administrator.dialogs.setprivatefailed";
		
		intArr["[setprivate]WAITING"]="administrator.dialogs.waitingchangevisflag";
		intArr["[setprivate]PLANNED"]="administrator.dialogs.setpublicrunning";
		intArr["[setprivate]FAILED"]="administrator.dialogs.setpublicfailed";

		intArr["[export]WAITING"]="administrator.dialogs.waitingexport";
		intArr["[export]PLANNED"]="administrator.dialogs.exportrunning";
		intArr["[export]FAILED"]="administrator.dialogs.exportfailed";

		intArr["[convert]WAITING"]="administrator.dialogs.waitingconvert";
		intArr["[convert]PLANNED"]="administrator.dialogs.convertrunning";
		intArr["[convert]FAILED"]="administrator.dialogs.convertfailed";

		intArr["[import]WAITING"]="administrator.dialogs.waitingimport";
		intArr["[import]PLANNED"]="administrator.dialogs.importrunning";
		intArr["[import]FAILED"]="administrator.dialogs.importfailed";

		intArr["[generateDeepZoomTiles]WAITING"]="administrator.dialogs.waitinggenerateDeepZoomTiles";
		intArr["[generateDeepZoomTiles]PLANNED"]="administrator.dialogs.generateDeepZoomTilesrunning";
		intArr["[generateDeepZoomTiles]FAILED"]="administrator.dialogs.generateDeepZoomTilesfailed";

        intArr["[deleteGeneratedDeepZoomTiles]WAITING"]="administrator.dialogs.waitingdeleteGeneratedDeepZoomTiles";
        intArr["[deleteGeneratedDeepZoomTiles]PLANNED"]="administrator.dialogs.deleteGeneratedDeepZoomTilesrunning";
        intArr["[deleteGeneratedDeepZoomTiles]FAILED"]="administrator.dialogs.deleteGeneratedDeepZoomTilesfailed";
	}
	return intArr;
}(); //akce ze servletu




function importMonographs() {
    showConfirmDialog('Confirm import monografii', function(){
		var url = "lr?action=start&def=replikator_monographs&out=text";
		if (_commonDialog) {

			$("#common_started_ok").hide();
	    	$("#common_started_failed").hide();
	    	$("#common_started_waiting").show();

	    	_commonDialog.dialog('open');
		} else {
	
	    	$("#common_started_waiting").show();
	    	_commonDialog = $("#common_started").dialog({
		        bgiframe: true,
		        width: 400,
		        height: 100,
		        modal: true,
		        title:'',
		        buttons: {
		            "Close": function() {
		                $(this).dialog("close"); 
		            } 
		        } 
		    });
		}
		$("#common_started_text").text(dictionary['administrator.dialogs.waitingmonographimport']);
		$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.importMonograph.title']);
	
		_startProcess(url);
    });
}


function importPeriodicals() {
    showConfirmDialog('Confirm import periodik', function(){
		var url = "lr?action=start&def=replikator_periodicals&out=text";

		if (_commonDialog) {

			$("#common_started_ok").hide();
	    	$("#common_started_failed").hide();
	    	$("#common_started_waiting").show();
	
	    	_commonDialog.dialog('open');
		} else {
	    	$("#common_started_waiting").show();
	    	_commonDialog = $("#common_started_waiting").dialog({ 
		        bgiframe: true,
		        width: 400,
		        height: 100,
		        modal: true,
		        title:'',
		        buttons: {
		            "Close": function() {
		                $(this).dialog("close"); 
		            } 
		        } 
		    });
		}

		$("#common_started_text").text(dictionary['administrator.dialogs.waitingperiodicsimport']);
		$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.importPeriodical.title']);

		_startProcess(url);
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
    if (_commonDialog) {

	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();

    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title:'',
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	
	}
    
	$("#common_started_text").text(dictionary['administrator.dialogs.waitingreindex']);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.reindex.title']);

	_startProcess(url);
}

function _startProcess(url) {
	$.get(url, function(data) {
		var text = _texts[data];
		var t = dictionary[text];
                //alert(data);
		if (data.match("PLANNED$")=="PLANNED") {
			_processTextOk(t);
			setTimeout(_processStarted, 3000);
		} else {
			_processFailed(t);
			setTimeout(_processFailed, 3000);
		}

	});
}



function exportTOFOXML(level)  {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	var pidpath = COMMON.pidpath(level);
	var url = "lr?action=start&def=export&out=text&params="+pid;
	if (_commonDialog) {
    	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();
    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: '',
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	$("#common_started_text").text(dictionary['administrator.dialogs.waitingfoexport']);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.foexport.title']);

	_startProcess(url);
}


function noParamsProcess(process)  {
	var url = "lr?action=start&def="+process+"&out=text";
	if (_commonDialog) {
    	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();
    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        //height: 100,
	        modal: true,
	        title: '',
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	$("#common_started_text").text(dictionary['administrator.dialogs.waiting'+process]);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.'+process+'.title']);
	_startProcess(url);
}



function deleteUuid(level, model)  {
	hideAdminOptions(level);
	showConfirmDialog(dictionary['administrator.dialogs.deleteconfirm'], function(){
		var pid = $("#tabs_"+level).attr('pid');
		var pidpath = COMMON.pidpath(level);
		var url = "lr?action=start&def=delete&out=text&params="+pid+","+pidpath;
		if (_commonDialog) {
	    	$("#common_started_ok").hide();
	    	$("#common_started_failed").hide();
	    	$("#common_started_waiting").show();
	    	_commonDialog.dialog('open');
		} else {
	    	$("#common_started_waiting").show();
	    	_commonDialog = $("#common_started").dialog({
		        bgiframe: true,
		        width: 400,
		        height: 100,
		        modal: true,
		        title: '',
		        buttons: {
		            "Close": function() {
		                $(this).dialog("close"); 
		            } 
		        } 
		    });
		}
	
		$("#common_started_text").text(dictionary['administrator.dialogs.waitingdelete']);
		$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.deleteuuid']);
	
		_startProcess(url);
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
	if (_commonDialog) {
    	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();
    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
	
	$("#common_started_text").text(dictionary['administrator.dialogs.waitingstaticPDF']);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.staticPDF.title']);

	_startProcess(url);
}

/**
 * Enumerator
 * @param level
 * @return
 */
var _commonDialog; //cekaci dialog na spusteni procesu


function _processTextOk(text) {
	$("#common_started_text_ok").text(text);
}

function _processStarted() {
	$("#common_started_ok").show();
	$("#common_started_failed").hide();
	$("#common_started_waiting").hide();
}

function _processTextFailed(text) {
	$("#common_started_text_failed").text(text);
}

function _processFailed() {
	$("#common_started_waiting").css("display","none");
	$("#common_started_ok").css("display","block");

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
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
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


function generateDeepZoomTiles(level, model) {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');

  	if (_commonDialog) {
    	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();
    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: '',
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	$("#common_started_text").text(dictionary['administrator.dialogs.waitinggenerateDeepZoomTiles']);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.generateDeepZoomTiles.title']);

	var url = "lr?action=start&def=generateDeepZoomTiles&out=text&params="+pid;
	_startProcess(url);
}

function deleteGeneratedDeepZoomTiles(level, model) {
    hideAdminOptions(level);
    var pid = $("#tabs_"+level).attr('pid');

    if (_commonDialog) {
        $("#common_started_ok").hide();
        $("#common_started_failed").hide();
        $("#common_started_waiting").show();
        _commonDialog.dialog('open');
    } else {
        $("#common_started_waiting").show();
        _commonDialog = $("#common_started").dialog({
            bgiframe: true,
            width: 400,
            height: 100,
            modal: true,
            title: '',
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }

    $("#common_started_text").text(dictionary['administrator.dialogs.waitingdeleteGeneratedDeepZoomTiles']);
    $("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.deleteGeneratedDeepZoomTiles.title']);

    var url = "lr?action=start&def=deleteGeneratedDeepZoomTiles&out=text&params="+pid;
    _startProcess(url);
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

  	if (_commonDialog) {
    	$("#common_started_ok").hide();
    	$("#common_started_failed").hide();
    	$("#common_started_waiting").show();
    	_commonDialog.dialog('open');
	} else {
    	$("#common_started_waiting").show();
    	_commonDialog = $("#common_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: '',
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	$("#common_started_text").text(dictionary['administrator.dialogs.waitingdelindex']);
	$("#common_started" ).dialog( "option", "title",  dictionary['administrator.menu.dialogs.delindex.title']);

	var url = "lr?action=start&def=reindex&out=text&params=deleteDocument,"+pid_path+","+pid;
      _startProcess(url);
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
          
          _startProcess(url);
        });        
    }).error(function(){
        alert("PID not found");
    });
}

function confirmIndexModel(model){
    showConfirmDialog('Confirm index model: ' + model, function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      _startProcess(url);
    });
}

function checkIndexIntegrity(){
var text = dictionary['administrator.dialogs.confirm'] + " " + dictionary['administrator.menu.dialogs.check_integrity'];
    showConfirmDialog(text, function(){
      var url = "lr?action=start&def=reindex&out=text&params=checkIntegrity,check,Check integrity";
      _startProcess(url);
    });
}

function indexDoc(pid, title){
    showConfirmDialog('Confirm index dokumentu', function(){
    var prefix = "info\:fedora\/";
    var pid2 = pid.replace(prefix,"");
      var escapedTitle = replaceAll(title, ',', '');
      var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+pid2+","+escapedTitle;
      _startProcess(url);
    });
}

function indexModel(model){
    showConfirmDialog('Confirm index cely model', function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      _startProcess(url);
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
                buttons: {
                    "Aplikuj": bind(function() {
                        this.dialog.dialog("close");
                        this.startProcess();                        
                     },this),
                    "Close": function() {
                        $(this).dialog("close"); 
                    } 
                }
            });
                
        }
        $("#changeflagDialog").html(data);
        
    },this));
}

var changeFlag = new ChangeFlag();

</script>