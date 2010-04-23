/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var _processDialog; // dialog na zobrazovani proceus
function processes(){
	if (_processDialog) {
    	_processDialog.dialog('open');
	} else {
    	_processDialog = $("#processes").dialog({
	        bgiframe: true,
	        width: 600,
	        height: 200,
	        modal: true,
	        title: "Staticky export do PDF",
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        } 
	    });
	}
}

/**
 * Promenne ve scriptu
 */
var _waitingDialog; //cekaci dialog na spusteni procesu
var _actions=function() {
	var intArr = new Array(); {
		intArr["RUNNING"]=_showProcessStarted;
		intArr["FAILED"]=_showProcessFailed;
	}
	return intArr;
}(); //akce ze servletu


/**
 * Generovani staticke exportu
 * @param level
 * @return
 */
function generateStatic(level, exportType){
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def="+exportType+"&out=text&params="+pid;
	if (_waitingDialog) {
    	$("#process_started_ok").hide();
    	$("#process_started_failed").hide();
    	$("#process_started_waiting").show();
    	_waitingDialog.dialog('open');
	} else {
    	$("#process_started_waiting").show();
		_waitingDialog = $("#process_started").dialog({
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

	$.get(url, function(data) {
		var action  = _actions[data];
		if (action) {
			setTimeout(action, 3000);
		}
                return true;
	});
        
}

/**
 * Nastavuje stav procesu na ok
 * @return
 */
function _showProcessStarted() {
	$("#process_started_waiting").css("display","none");
	$("#process_started_ok").css("display","block");
}
/**
 * Nastavuje stav procesu na fail
 * @return
 */
function _showProcessFailed() {
	$("#process_started_waiting").css("display","none");
	$("#process_started_failed").css("display","block");
}

