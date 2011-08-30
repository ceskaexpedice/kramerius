$(function(){
    $('button').button();
    $('.facet').addClass('shadow10 ui-tabs ui-widget ui-widget-content ui-corner-all');
    $('.facet>div').addClass('ui-dialog-titlebar ui-widget-header');

});

function jq(myid) { 
    return '#' + myid.replace(/(:|\.|\/)/g,'\\$1');
}

//Toggle visibility advanced search option
function toggleAdv(){
    var y = $('#q').offset().top + $('#q').height() + 10;
    var x = $('#q').offset().left;
    $('#advSearch').css('left', x);
    $('#advSearch').css('top', y);
    $('#advSearch').toggle();
}

function trim10(str) {
    var whitespace = ' \n\r\t\f\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000';
    for (var i = 0; i < str.length; i++) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(i);
            break;
        }
    }
    for (i = str.length - 1; i >= 0; i--) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(0, i + 1);
            break;
        }
    }
    return whitespace.indexOf(str.charAt(0)) === -1 ? str : '';
}

function isScrolledIntoView(elem, view){
    var docViewTop = $(view).scrollTop();
    var docViewBottom = docViewTop + $(view).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

function isScrolledIntoWindow(elem){
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}



/** construct url from selected pids
 */
function urlWithPids(/** String */baseUrl, /** Array */ pids) {
	if (!pids) pids = this.pids;
	return baseUrl+"{"+reduce(function(base, item, status) {
		
		base = base+item.pid.replaceAll(":","\\:")+ (status.last ? "": ";");
        return base;
    }, "",pids)+"}";        
}


/** Download original */
function DownloadOriginal() {
	this.dialog = null;
}

DownloadOriginal.prototype.download = function(structs) {

    var urlDialog=urlWithPids("inc/_download_original_dialog.jsp?pids=",structs);
    $.get(urlDialog, function(data){
    	if (this.dialog) {
    		this.dialog.dialog('open');
	    } else {
	        $(document.body).append('<div id="downloadOriginal"></div>')
	        this.dialog = $('#downloadOriginal').dialog({
	            width:600,
	            height:400,
	            modal:true,
	            title: dictionary["administrator.menu.downloadOriginal"],
	            buttons: {
	            	"Close": function() {
	                    $(this).dialog("close");
	                }
	            }
	        });
	    }
    	
    	$("#downloadOriginal").html(data);
    });

}
/** Download original object */
var downloadOriginal = new DownloadOriginal();


/**  PDF */
function PDF() {
	this.dialog = null;
	this.structs = null;
	this.previous = null;
}


PDF.prototype.renderPDF = function() {
	var u = null;
	var selected = $("#pdf input:checked");
	if (selected.length >= 1) {
		var pidsstring = selected.val();
		var id = selected.attr("id"); id = id.substring(0, id.length - "_radio".length);
		if (id == "selection") {
			var selectedPids = pidsstring.slice(1,pidsstring.length-1).split(",");
			selectedPids = map(function(elm) {
				return {model:'',pid:elm.trim() }	
			},selectedPids);
			u = urlWithPids("pdf?action=SELECTION&pids=",selectedPids);
		} else {
			var selectedPids = pidsstring.slice(1,pidsstring.length-1).split(",");
			var howMany = parseInt($("#"+id+"_input").val());
			u = "pdf?action=PARENT&pidFrom="+selectedPids[0]+"&howMany="+howMany;
		}
		u = u +"&redirectURL="+ escape(window.location.href);
		window.location.href = u;
	} else {
		 throw new Error("No pdf option selected !");
	}
}

PDF.prototype.generate = function(objects) {
	this.structs = objects;
	var urlDialog=urlWithPids("inc/_pdf_dialog.jsp?pids=",objects);
	$.get(urlDialog, bind(function(data){

		if (this.dialog) {
	    		this.dialog.dialog('open');
    	} else {
            $(document.body).append('<div id="pdf"></div>')
            this.dialog = $('#pdf').dialog({
                width:600,
                height:500,
                modal:true,
                title: dictionary["generatePdfTitle"],
                buttons: {
                    "Generuj": bind(function() {
                    	this.renderPDF();
                    	this.dialog.dialog("close");
                    },this),

                	"Close": function() {
                        $(this).dialog("close");
                    }
                }
            });
		}
		
		
		$('#pdf').html(data);
		
	}, this));
}




PDF.prototype.onKeyup=function(id,type,pidsstring) {
	var val = $("#"+id+"_input").val();
	if (!isNaN(val)) {
		var n=parseInt($("#"+id+"_input").val());;
		if (n <= k4Settings.pdf.generatePdfMaxRange) {
			$("#"+id+"_error").text("");
		} else {
			$("#"+id+"_error").text(dictionary["pdf.validationError.toomuch"]);
		}
	} else {
		$("#"+id+"_error").text(dictionary["pdf.validationError.nan"]);
	}
}

PDF.prototype.onChange = function(id,type,pidsstring) {
	if (this.previous) {
		$(this.previous).hide();
	}
	$("#"+id+"_option").show();
 	this.previous = "#"+id+"_option";
}

/** PDF object */
var pdf = new PDF();


/** Print object */
function Print() {
	this.dialog = null;
	this.structs = null;
	this.previous = null;
	
	this.informDialog = null;
}


Print.prototype.printTitle = function() {
	var u = null;
	var selected = $("#print input:checked");
	if (selected.length >= 1) {

		var pidsstring = selected.val();
		var id = selected.attr("id"); id = id.substring(0, id.length - "_radio".length);
		if (id == "selection") {
			var selectedPids = pidsstring.slice(1,pidsstring.length-1).split(",");
			selectedPids = map(function(elm) {
				return {model:'',pid:elm.trim() }	
			},selectedPids);
			u = urlWithPids("print?action=SELECTION&pids=",selectedPids);
		} else {
			var selectedPids = pidsstring.slice(1,pidsstring.length-1).split(",");
			u = "print?action=PARENT&pidFrom="+selectedPids[0];
		}

		
		if (this.informDialog) {
    		this.informDialog.dialog('open');
		} else {
			$(document.body).append('<div id="printInfo"></div>')
            this.informDialog = $('#printInfo').dialog({
                width:200,
                height:120,
                modal:true,
                title: dictionary["administrator.dialogs.print"],
                buttons: {
                	"Close": function() {
                        $(this).dialog("close");
                    }
                }
            });
			
		}
        $(document.body).append('<div id="printInfo"></div>');

        
		$('#printInfo').html(
				'<table><tr><td aling="center" halign="center">'+
				dictionary['print.info.waiting.message']+
				'</td></tr></table>'
		);

		$.get(u, bind(function(data){
			$('#printInfo').html(
				'<table><tr><td aling="center" halign="center">'+
				dictionary['print.info.done.message']+
				'</td></tr></table>');

		}, this));
		
	} else {
		 throw new Error("No print option selected !");
	}
}

Print.prototype.onChange = function(id,type,pidsstring) {
	this.selectedType = type;
	this.selectedPids = pidsstring.slice(1,pidsstring.length-1).split(",");
}

Print.prototype.print = function(objects) {
	this.structs = objects;
	var urlDialog=urlWithPids("inc/_print_dialog.jsp?pids=",objects);
	$.get(urlDialog, bind(function(data){
		if (this.dialog) {
    		this.dialog.dialog('open');
    	} else {
            $(document.body).append('<div id="print"></div>')
            this.dialog = $('#print').dialog({
                width:600,
                height:500,
                modal:true,
                title: dictionary["administrator.dialogs.print"],
                buttons: {
                    "Generuj": bind(function() {
                    	this.printTitle();
                    	this.dialog.dialog("close");
                    },this),

                	"Close": function() {
                        $(this).dialog("close");
                    }
                }
            });
		}
		$('#print').html(data);
		
	}, this));
}

var print = new Print();

