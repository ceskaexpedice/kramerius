
function ProcessessFilter() {
	this.filter = [];
}


ProcessessFilter.prototype.apply=function(ordering, size, type) {
	this.filter = [];
	$(".filter-vals").each(bind(function(i,val) {
		if ($(val).val()) {
			if ($(val).hasClass("eq")) {
				this.filter.push({name:$(val).attr('name'),op:"EQ",val:$(val).val()});
			} else if ($(val).hasClass("lt")) {
				this.filter.push({name:$(val).attr('name'),op:"LT",val:$(val).val()});
			} else if ($(val).hasClass("gt")) {
				this.filter.push({name:$(val).attr('name'),op:"GT",val:$(val).val()});
			} else if ($(val).hasClass("like")) {
				var rval = $(val).val();
				if (!rval.startsWith("%")) {
					rval = "%"+rval; 
				}
				if (!rval.endsWith("%")) {
					rval = rval+"%"; 
				}
				rval = escape(rval);
				this.filter.push({name:$(val).attr('name'),op:"LIKE",val:rval});
			}
		}
	},this));
	

	processes.wait();
	var url = "inc/admin/_processes_data.jsp?ordering="+ordering+"&size="+size+"&type="+type+this.filterPostfix();
	$.get(url, function(data) {
		$("#processes").html(data);
		processes.repairDisplayed();
	});
	
	$(".filter").toggle();
    $(".displayButton").toggle();
}


ProcessessFilter.prototype.close=function() {
	$(".filter").toggle();
    $(".displayButton").toggle();
}


ProcessessFilter.prototype.filterPostfix = function() {
	if (this.filter) {
		var furl = this.curl();
		return "&filter="+this.curl();
	} else return "";
}

ProcessessFilter.prototype.curl=function() {
	return encodeURI("{")+reduce(function(base, item, status) {
    	base = base+reduceItem(item)+ (status.last ? "": ";");
        return base;
    }, "",this.filter)+encodeURI("}");
	
	function reduceItem(item) {
		return encodeURI("{")+item.name+";"+item.op+";"+encodeURI(item.val.replaceAll(":","\\:"))+encodeURI("}");
	}
}


function Processes() {
	this.dialog = null;
	this.displayedRows = [];
	
	this.currentFilter = new ProcessessFilter();
}

Processes.prototype.repairDisplayed = function() {
	$(this.displayedRows).each(function(index, val) {
		$("."+val).show();
        $("#"+val+"_icon").attr("src","img/nolines_minus.gif");
	});
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
	        buttons: [{
	                	  text:dictionary['common.close'],
	                	  click:function() {
	        	        	 $(this).dialog("close"); 
	                	  }
        	}]
		
	    });
	}
}


Processes.prototype.processes = function (){
	var url = "inc/admin/_processes_data.jsp?size=20&ordering=PLANNED&type=DESC";
		this.openProcessDialog();
		this.dialog.dialog('option', 'position', [10, 10]);
		this.dialog.dialog("option", "width", $(window).width()-20);
		this.dialog.dialog("option", "height", $(window).height()-60);
		$("#processes>table").css('height',$(window).height()-160);
		this.wait();
		$.get(url, bind(function(data) {
			$("#processes").html(data);;
		}, this));
}

Processes.prototype.wait = function() {
	   $("#processes").html('<div style="margin-top:30px;width:100%;text-align:center;"><img src="img/loading.gif" alt="loading" /></div>');
}



Processes.prototype.modifyProcessDialogDataByPage = function(ordering, page, size, type) {
	this.wait();
	var url = "inc/admin/_processes_data.jsp?ordering="+ordering+ (page != null ? "&page="+page : "") +"&size="+size+"&type="+type+this.currentFilter.filterPostfix();
	$.get(url, bind(function(data) {
		$("#processes").html(data);
	    this.repairDisplayed();
	},this));

}

Processes.prototype.doActionAndRefresh=function(url,ordering, page, size, type) {
	$.get(url, bind(function(fdata) {
		this.refreshProcesses(ordering, page, size, type);
	},this));
}

Processes.prototype.refreshProcesses = function(ordering, page, size, type) {
	this.wait();
	var refreshurl = "inc/admin/_processes_data.jsp?ordering="+ordering+"&page="+page+"&size="+size+"&type="+type;
	$.get(refreshurl, function(sdata) {
		$("#processes").html(sdata);
	});
}

Processes.prototype.subprocesses = function(id) {
	if (this.displayedRows.indexOf(id) >= 0) {
        $("."+id).hide();
        $("#"+id+"_icon").attr("src","img/nolines_plus.gif");
        this.displayedRows.rm(this.displayedRows.indexOf(id));
    } else {
        $("."+id).show();
        $("#"+id+"_icon").attr("src","img/nolines_minus.gif");
        this.displayedRows.push(id);
    }
}

var processes = new Processes();



