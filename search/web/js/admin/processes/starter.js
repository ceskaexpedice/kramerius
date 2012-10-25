var PROCESSES = {};

function Starter(processName, keys) {
	this.processName = processName;
	this.dialog = null;
	this.keys = keys;
	this.callback = null;
}

//TODO: do it better 
Starter.prototype.notify = function() {
    if (this.callback) {
        this.callback.apply(this, arguments);
    }
}

Starter.prototype.start=function(url, fc) {
    this.callback = fc;
    if (this.dialog) {
		this.dialog.dialog('open');
	} else {
		var prid = "pr"+this.processName;
		var pdiv = '<div id="'+prid+'"></div>';

		$(document.body).append(pdiv);
        
        this.dialog = $("#"+prid).dialog({
	        bgiframe: true,
	        width:  400,
	        height:  200,
	        modal: true,
	        title: dictionary['administrator.menu.dialogs.lrprocesses.title'],
	        buttons: [{
	                   text:dictionary['common.close'],
	                   click:  bind(function() {
	                       this.dialog.dialog("close"); 
                           this.notify();
	                   }, this)
	               }]
	    });
	}


	this.waiting();
	
	$.get(url, bind(function(data) {
		if (data.match("PLANNED$")=="PLANNED") {
			setTimeout(bind(this.started,this), 3000);
		} else {
			setTimeout(bind(this.failed,this), 3000);
		}
	},this));
}

Starter.prototype.waitinghtml = function() {
	var key = "administrator.dialogs.waiting"+this.processName;
	var text = dictionary[key];
	var html =
    	"<div style=\"margin: 16px; font-family: sans-serif; font-size: 10px;\">"+
    		"<table style='width:100%'>"+
    			"<tbody><tr><td align=\"center\"><img src=\"img/loading.gif\" height=\"16px\" width=\"16px\"></td></tr>"+
    			"<tr><td align=\"center\" id=\"common_started_text\">"+text+"</td></tr>"+
    			"</tbody>" +
			"</table>" +
		"</div>";
    return html;
}

Starter.prototype.startedhtml = function() {
    var html =
    	"<div style=\"margin: 16px; font-family: sans-serif; font-size: 10px;\">"+
    		"<table style='width:100%'>"+
    			"<tbody>"+
    			"<tr><td align=\"center\" id=\"common_started_text\">"+dictionary["administrator.dialogs."+this.processName+"running"]+"</td></tr>"+
    			"</tbody>" +
			"</table>" +
		"</div>";
    return html;
	
}

Starter.prototype.failedhtml = function() {
    var html =
    	"<div style=\"margin: 16px; font-family: sans-serif; font-size: 10px;\">"+
    		"<table  style='width:100%'>"+
    			"<tbody>"+
    			"<tr><td align=\"center\" id=\"common_started_text\">"+dictionary["administrator.dialogs."+this.processName+"failed"]+"</td></tr>"+
    			"</tbody>" +
			"</table>" +
		"</div>";
	
    return html;
}


Starter.prototype.waiting=function() {
	$("#pr"+this.processName).html(this.waitinghtml());
}

Starter.prototype.started=function() {
	$("#pr"+this.processName).html(this.startedhtml());
    this.notify();
}
Starter.prototype.failed=function() {
    $("#pr"+this.processName).html(this.failedhtml());
    this.notify();
}


function processStarter(name) {
	if (!PROCESSES[name]) {
		PROCESSES[name] = new Starter(name);
	}
	return PROCESSES[name];
}


function pidstructs() {
	var pids = getAffectedPids();
	var structs = map(function(pid) { 
	    var divided = pid.split("_");            
	    var structure = {
	               models:divided[0],
	               pid:divided[1]
	        };
	    return structure;            
	}, pids);     
	return structs;
}

