/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function(){
    $('body').click(function() {
    	hideAdminMenu();
    });
});




function showAdminMenu() {
	var headerPosition = $("#header").offset();
	var headerWidth = $("#header").width()
	var admimMenuWidth = $("#adminMenu").width();
	
	var position = $("#adminHref").offset();
	
	
	$("#adminMenu").css("left",(headerPosition.left+headerWidth) - admimMenuWidth-2);
	$("#adminMenu").css("top",position.top);
	$("#adminMenu").css("display","block");
}

function hideAdminMenu() {
	$("#adminMenu").css("display","none");
}




var _processDialog; // dialog na zobrazovani proceus
function openProcessDialog() {
	if (_processDialog) {
		_processDialog.dialog('open');
	} else {
    	_processDialog = $("#processes").dialog({
	        bgiframe: true,
	        width: 700,
	        height: 400,
	        modal: true,
	        title: "Správa dlouhotrvajícíh procesů",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
}
function processes(){
	var url = "dialogs/_processes_data.jsp?ordering=NAME&offset=0&size=20&type=DESC";
	$.get(url, function(data) {
		openProcessDialog();
		$("#processes").html(data);
	});
}

function modifyProcessDialogData(ordering, offset, size, type) {
	var url = "dialogs/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;
	$.get(url, function(data) {
		$("#processes").html(data);
	});
}

function killAndRefresh(url,ordering, offset, size, type) {
	$.get(url, function(fdata) {
		refreshProcesses(ordering, offset, size, type);
	});
}

function refreshProcesses(ordering, offset, size, type) {
	alert("Refresh ... ");
	var refreshurl = "dialogs/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;
	$.get(refreshurl, function(sdata) {
		$("#processes").html(sdata);
	});
	
}

/**
 * Promenne ve scriptu
 */
// Command pattern
var _actions=function() {
	var intArr = new Array(); {
		intArr["[static_export_CD]RUNNING"]=_statitExportStarted;
		intArr["[static_export_CD]FAILED"]=_staticExportFailed;

		intArr["[reindex]RUNNING"]=_reindexStarted;
		intArr["[reindex]FAILED"]=_reindexFailed;

		intArr["[replikator_monographs]RUNNING"]=_replikatorMonographStarted;
		intArr["[replikator_monographs]FAILED"]=_replikatorMonographFailed;

		intArr["[replikator_periodicals]RUNNING"]=_replikatorPeriodicalStarted;
		intArr["[replikator_periodicals]FAILED"]=_replikatorPeriodicalFailed;
		
		intArr["[enumerator]RUNNING"]=_enumeratorStarted;
		intArr["[enumerator]FAILED"]=_enumeratorFailed;
				
		intArr["[replicationrights]RUNNING"]=_replicationrightsStarted;
		intArr["[replicationrights]FAILED"]=_replicationrightsFailed;

	}
	return intArr;
}(); //akce ze servletu


var _monographsDialog;
function importMonographs() {
	var url = "lr?action=start&def=replikator_monograph&out=text";
	if (_monographsDialog) {
    	$("#replikator_monograph_started_ok").hide();
    	$("#replikator_monograph_started_failed").hide();
    	$("#replikator_monograph_started_waiting").show();
    	_monographsDialog.dialog('open');
	} else {
    	$("#replikator_monograph_started_waiting").show();
    	_monographsDialog = $("#replikator_monograph_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Replikace .. ",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	_startProcess(url);
}


var _monographsDialog;
function importMonographs() {
	var url = "lr?action=start&def=replikator_monographs&out=text";
	if (_monographsDialog) {
    	$("#replikator_monograph_started_ok").hide();
    	$("#replikator_monograph_started_failed").hide();
    	$("#replikator_monograph_started_waiting").show();
    	_monographsDialog.dialog('open');
	} else {
    	$("#replikator_monograph_started_waiting").show();
    	_monographsDialog = $("#replikator_monograph_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Replikace .. ",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	_startProcess(url);
}

var _periodicalsDialog;
function importPeriodicals() {
	var url = "lr?action=start&def=replikator_periodicals&out=text";
	if (_periodicalsDialog) {
    	$("#replikator_periodical_started_ok").hide();
    	$("#replikator_periodical_started_failed").hide();
    	$("#replikator_periodical_started_waiting").show();
    	_periodicalsDialog.dialog('open');
	} else {
    	$("#replikator_periodical_started_waiting").show();
    	_periodicalsDialog = $("#replikator_periodical_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Replikace .. ",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	_startProcess(url);
}

/**
 * Reindexace
 * @param level
 * @return
 */
var _reindexDialog = null;
function reindex(level) {
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def=reindex&out=text&params=params=-action,fromKrameriusModel,-pid,"+pid;
	if (_reindexDialog) {
    	$("#reindex_started_ok").hide();
    	$("#reindex_started_failed").hide();
    	$("#reindex_started_waiting").show();
    	_reindexDialog.dialog('open');
	} else {
    	$("#reindex_started_waiting").show();
    	_reindexDialog = $("#reindex_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Reindexace ... ",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}

	_startProcess(url);
}

function _startProcess(url) {
	$.get(url, function(data) {
		var action  = _actions[data];
		if (action) {
			setTimeout(action, 3000);
		} else  {
			alert("Neznama data "+data);
		}
        return true;
	});
}

var _replicationrightsDialog; //cekaci dialog na spusteni procesu
function replicationrights() {
	var url = "lr?action=start&def=replicationrights&out=text";

	if (_staticExportDialog) {
    	$("#replicationrights_started_ok").hide();
    	$("#replicationrights_started_failed").hide();
    	$("#replicationrights_started_waiting").show();
    	_staticExportDialog.dialog('open');
	} else {
    	$("#replicationrights_started_waiting").show();
		_staticExportDialog = $("#replicationrights_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Replication rights",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
	_startProcess(url);
	
}



/**
 * Generovani staticke exportu
 * @param level
 * @return
 */
var _staticExportDialog; //cekaci dialog na spusteni procesu
function generateStatic(level, exportType){
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def="+exportType+"&out=text&params="+pid;
	if (_staticExportDialog) {
    	$("#process_started_ok").hide();
    	$("#process_started_failed").hide();
    	$("#process_started_waiting").show();
    	_staticExportDialog.dialog('open');
	} else {
    	$("#process_started_waiting").show();
		_staticExportDialog = $("#process_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Staticky export do PDF",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
	_startProcess(url);
}

/**
 * Enumerator
 * @param level
 * @return
 */
var _enumeratorDialog; //cekaci dialog na spusteni procesu
function enumerator(){
	var url = "lr?action=start&def=enumerator&out=text";
	if (_enumeratorDialog) {
    	$("#enumerator_started_ok").hide();
    	$("#enumerator_started_failed").hide();
    	$("#enumerator_started_waiting").show();
    	_enumeratorDialog.dialog('open');
	} else {
    	$("#enumerator_started_waiting").show();
    	_enumeratorDialog = $("#enumerator_started").dialog({
	        bgiframe: true,
	        width: 400,
	        height: 100,
	        modal: true,
	        title: "Enumerator",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
	_startProcess(url);
}

function _statitExportStarted() {
	$("#process_started_waiting").css("display","none");
	$("#process_started_ok").css("display","block");
}
function _staticExportFailed() {
	$("#process_started_waiting").css("display","none");
	$("#process_started_failed").css("display","block");
}

function _reindexStarted() {
	$("#reindex_started_waiting").css("display","none");
	$("#reindex_started_ok").css("display","block");
}

function _reindexFailed() {
	$("#reindex_started_waiting").css("display","none");
	$("#reindex_started_ok").css("display","block");
}


function _replikatorMonographStarted() {
	$("#replikator_monograph_started_waiting").css("display","none");
	$("#replikator_monograph_started_ok").css("display","block");
}

function _replikatorMonographFailed() {
	$("#replikator_monograph_started_waiting").css("display","none");
	$("#replikator_monograph_started_ok").css("display","block");
}

function _replikatorPeriodicalStarted() {
	$("#replikator_periodical_started_waiting").css("display","none");
	$("#replikator_periodical_started_ok").css("display","block");
}

function _replikatorPeriodicalFailed() {
	$("#replikator_periodical_started_waiting").css("display","none");
	$("#replikator_periodical_started_ok").css("display","block");
}


function _enumeratorStarted() {
	$("#enumerator_started_waiting").css("display","none");
	$("#enumerator_started_ok").css("display","block");
}

function _enumeratorFailed() {
	$("#enumerator_started_waiting").css("display","none");
	$("#enumerator_started_ok").css("display","block");
}

function _replicationrightsStarted() {
	$("#replicationrights_started_waiting").css("display","none");
	$("#replicationrights_started_ok").css("display","block");
}

function _replicationrightsFailed() {
	$("#replicationrights_started_waiting").css("display","none");
	$("#replicationrights_started_ok").css("display","block");
}


var _indexerDialog;
/**
 * Zobrazuje spravu indexace
 */
function showIndexerAdmin(){
    hideAdminMenu();
    var url = "dialogs/_indexer_data.jsp?model=monograph&offset=0";
    $.get(url, function(data) {
        $("#indexerContent").html(data);
    });
    if (_indexerDialog) {
        _indexerDialog.dialog('open');
    } else {
    	_indexerDialog = $("#indexer").dialog({
            bgiframe: true,
            width: 700,
            height: 400,
            modal: true,
            title: "Indexace dokument�",
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
}

function loadFedoraDocuments(model, offset){
    var url = "dialogs/_indexer_data.jsp?model="+model+"&offset="+offset;
    $.get(url, function(data) {
        $("#indexerContent").html(data);
    });
}

function indexDoc(pid, title){
    var prefix = "info:fedora/uuid:";
    var uuid = pid.substr(prefix.length);
    var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+uuid+","+title;
    $.get(url, function(data) {
        alert(data);
    });
}

function indexModel(model){
    var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
    $.get(url, function(data) {
        alert(data);
    });
}