
function Processes() {
	this.dialog = null;
	this.displayedRows = [];
}

Processes.prototype.openProcessDialog = function() {
	if (this.dialog) {
		this.dialog.dialog('open');
	} else {
		this.dialog = $("#processes").dialog({
	        bgiframe: true,
	        width:  $(window).width()-20,
	        height:  $(window).height()-60,
	        modal: true,
	        title: dictionary['administrator.menu.dialogs.lrprocesses.title'],
	        buttons: {
	            "Close": function() {
	                $(this).dialog("close"); 
	            } 
	        }
	    });
	}
}


Processes.prototype.processes = function (){
	var url = "inc/admin/_processes_data.jsp?offset=0&size=20&type=DESC";
	$.get(url, bind(function(data) {
		this.openProcessDialog();
		this.dialog.dialog('option', 'position', [10, 10]);
		this.dialog.dialog("option", "width", $(window).width()-20);
		this.dialog.dialog("option", "height", $(window).height()-60);
		$("#processes>table").css('height',$(window).height()-160)
		$("#processes").html(data);;
	}, this));
}


Processes.prototype.modifyProcessDialogData = function(ordering, offset, size, type) {
	var url = "inc/admin/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;
	$.get(url, function(data) {
		$("#processes").html(data);
	});
}

Processes.prototype.doActionAndRefresh=function(url,ordering, offset, size, type) {
	$.get(url, bind(function(fdata) {
		this.refreshProcesses(ordering, offset, size, type);
	},this));
}

Processes.prototype.refreshProcesses = function(ordering, offset, size, type) {
	var refreshurl = "inc/admin/_processes_data.jsp?ordering="+ordering+"&offset="+offset+"&size="+size+"&type="+type;
	$.get(refreshurl, function(sdata) {
		$("#processes").html(sdata);
	});
}

Processes.prototype.subprocesses = function(id) {
    if (this.displayedRows.indexOf(id) >= 0) {
        $("#"+id).hide();
        $("#"+id+"_icon").attr("src","img/nolines_plus.gif");
        this.displayedRows.rm(this.displayedRows.indexOf(id));
    } else {
        $("#"+id).show();
        $("#"+id+"_icon").attr("src","img/nolines_minus.gif");
        this.displayedRows.push(id);
    }
}

var processes = new Processes();