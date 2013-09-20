$(function(){
    $('button').button();
    $('.facet').addClass('shadow10 ui-tabs ui-widget ui-widget-content ui-corner-all');
    $('.facet>div').addClass('ui-dialog-titlebar ui-widget-header');

});

//TODO: poznamka by PS - funguje pouze pro objekty nejnizsi urovne ??
function jq(myid) { 
    return '#' + myid.replace(/(:|\.|\/)/g,'\\$1');
}

function showHelp(language, part){
    var url = 'help/help.jsp?';
    if (part!=null && part!='')
     url=url+'#'+part;
     temp=window.open(url,'HELP','width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=200,top=20');
     temp.opener=this;
     temp.focus(); 
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
    var docViewTop = $(view).offset().top;
    var docViewBottom = docViewTop + $(view).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    //$("#test").html("elemBottom: "+elemBottom+
    //    " docViewTop: "+docViewTop+
   //     " elemTop: "+elemTop+
   //     " docViewBottom: "+docViewBottom);
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
	
	this.waitDialog = null;
	
	this.deviceSelection = {
			"standard" : function() {
				$("#pdfsettings_ereader").hide();
				this.devconf = null;
			},
			"ereader" : function() {
				$("#pdfsettings_ereader").show();
				// change this ?? 
				pdf.onFormatChange();
			}
	};

	
	this.rectangleSelections = {
		// a4 format 
		"a4": function() {
			return { 
				"pageSize":[595,842],
				"fontsSettings": {
					"logoFont": {
						"style": PDF.PDF_FONT_STANDARD,
						"size": 48
					},
					"infFont": {
						"style": PDF.PDF_FONT_BOLD,
						"size": 14
					}
				}
			}
		},
		"a5": function() {
			return {
				"pageSize":[420,595],
				"fontsSettings": {
					"logoFont": {
						"style": PDF.PDF_FONT_STANDARD,
						"size": 48
					},
					"infFont": {
						"style": PDF.PDF_FONT_BOLD,
						"size": 14
					}
				}
			};
		}	
	};


	this.rectangle = null;
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

		if (this.devconf) {
			var rectangle = this.devconf["pageSize"];
			var logoFont = this.devconf["fontsSettings"]["logoFont"];
			var infFont = this.devconf["fontsSettings"]["infFont"];
			u += "&rect="+rectangle[0]+","+rectangle[1]+"&logo={"+logoFont["style"]+";"+logoFont["size"]+"}&info={"+infFont["style"]+";"+infFont["size"]+"}&firstpageType=IMAGES";
		}
		


		if (this.waitDialog) {
			this.waitDialog.dialog('open');
		} else {
			$(document.body).append('<div id="waitPdf">'+
			'<div style="margin: 16px; font-family: sans-serif; font-size: 10px; ">'+
				'<table width="100%">'+
			    		'<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>'+
						'<tr><td align="center" id="waitPdf_message">'+dictionary['pdf.waitDialog.message']+'</td></tr>'+
			    	'</table>'+
				'</div>'+
			'</div>')
		    this.waitDialog = $('#waitPdf').dialog({
		    	width:400,
		    	height:270,
		    	modal:true,
		    	title: dictionary["generatePdfTitle"],
                buttons:[{
		                     text: dictionary['common.close'],
		                     click: bind(function() {
		                    	this.dialog.dialog("close");
		                     },this)
                     }
                ]
		    });
		}
		
		
		$.get(u, bind(function(data) {
			this.waitDialog.dialog("close");
			var obj = eval('(' + data + ')');
			if ("errorType" in obj) {
				window.location.href = obj.redirect+"?redirectURL="+obj.returnUrl;
			} else {
				window.location.href = 'pdf?action=FILE&pdfhandle='+obj.pdfhandle;
			}
		},this));
		
	} else {
		 throw new Error("No pdf option selected !");
	}
}

PDF.prototype.generate = function(objects) {
	this.devconf = null;
	
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
                buttons:[
                         {
                             text: dictionary['pdf.dialog.button.generate'],
                             click: bind(function() {
                             	this.renderPDF();
                            	this.dialog.dialog("close");
                             },this)
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
		
		
		$('#pdf').html(data);
		
	}, this));
}




PDF.prototype.onKeyup=function(id,type,pidsstring) {
	var val = $("#"+id+"_input").val();
	if (!isNaN(val)) {
		var n=parseInt($("#"+id+"_input").val());;
		if (n > k4Settings.pdf.generatePdfMaxRange) {
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

 	$(".pdfSelected").removeClass("pdfSelected");
 	$("#"+id).addClass("pdfSelected");
}

/** zmena nastaveni (Desktop | Reader ) */
PDF.prototype.onSettingsChange = function(type) {
	if(this.deviceSelection[type]) {
		var invf = this.deviceSelection[type];
		invf.call(this);
	}
}

PDF.prototype.onFormatChange = function() {
	var val = $("#pdfsettings_ereader select option:selected").val();
	if (this.rectangleSelections[val]) {
		var invf = this.rectangleSelections[val];
		this.devconf = invf.call(this);
	}
}

/** PDF object */
var pdf = new PDF();

// font constants
PDF.PDF_FONT_STANDARD = 0;
PDF.PDF_FONT_ITALIC = 1;
PDF.PDF_FONT_BOLD = 2;
PDF.PDF_FONT_BOLDITALIC = 3;


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



function RegisterUser() {
	this.dialog = null; this.checkDialog = null; this.badCaptchaDialog = null;
	
}


RegisterUser.prototype.badCaptcha = function() {
	if (this.badCaptchaDialog) {
		this.badCaptchaDialog.dialog('open');
	} else {
		$(document.body).append('<div id="badCaptcha"></div>');
		$("#badCaptcha").html('<div> <h3> '+dictionary['registeruser.message.badcaptchatitle']+'</h3><div> '+dictionary['registeruser.message.badcaptcha']+'</div></div>');
		this.badCaptchaDialog = $('#badCaptcha').dialog({
            width:400,
            height:250,
            modal:true,
            title: dictionary['registeruser.message.badcaptchadialogtitle'],
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
	
}

RegisterUser.prototype.emailCheck = function() {
	if (this.checkDialog) {
		this.checkDialog.dialog('open');
	} else {
		$(document.body).append('<div id="checkEmailRegisterUser"></div>');
		$("#checkEmailRegisterUser").html('<div> <h3> '+dictionary['registeruser.message.checkmailtitle']+'</h3><div> '+dictionary['registeruser.message.checkemail']+'</div></div>');

		this.checkDialog = $('#checkEmailRegisterUser').dialog({
            width:400,
            height:250,
            modal:true,
            title: dictionary['registeruser.message.checkemaildialogtitle'],
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
}




RegisterUser.prototype.register = function() {
	$.get("inc/_register_new_user.jsp", bind(function(data){
		if (this.dialog) {
    		this.dialog.dialog('open');
    	} else {
    		$(document.body).append('<div id="registerUser"></div>');
            
    		this.dialog = $('#registerUser').dialog({
                width:600,
                height:550,
                modal:true,
                title: dictionary["registeruser.title"],
                buttons: [
                	// create button
                	{
                        text: dictionary['common.create'],
                        click: bind(function() {
                        	// validation...
                        	if (regUserValidate.validate()) {
                        		var data = regUserValidate.grabData();
                       
                        		$.ajax({
                        				type:"POST",
                        				url:"users?action=registernew",
                        				data: {
                                        	'loginName':data.loginName,
                                        	'email':data.email,
                                        	'password':data.pswd,
                                        	'name':data.name,
                                        	'captcha':data.captcha
                                        },
                                        contentType: "application/x-www-form-urlencoded;charset=UTF-8",
                                        dataType:"json",
                                        success: bind(function() {
                                        		this.emailCheck();
                                        	},this),
                                        error:bind(function (data) {
                                        		var dataObject = eval('(' +data.responseText + ')');
                                        		if (dataObject && dataObject.error=='bad_captcha') {
                                        			this.badCaptcha();
                                        		}
                                        	},this)

                        		});
                        		
                                
                    			this.dialog.dialog("close");
                        	}
                        },this)

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
		
		$("#registerUser").html(data);
	},this));
	
}

var registerUser = new RegisterUser();


/* profile functionality */
function Profile() {}

Profile.prototype.modify = function(func,okfunc) {
    $.get("profile?action=GET", function(data) {
        data = func(data);
        var encodedData = Base64.encode(JSON.stringify(data));
        $.post("profile?action=POST",{'encodedData':encodedData},okfunc,"json");
    });
}


/** common message functionality */
function Message(id) {
    this.dialog = null;
    this.id = id;
}

Message.prototype.show=function() {
    if (this.dialog) {
        this.dialog.dialog('open');
    } else {
        $(document.body).append("<div id='message_dialog_"+this.id+"'></div>");
        this.dialog = $("#message_dialog_"+this.id).dialog({
            bgiframe: true,
            width: 350,
            height: 150,
            modal: true,
            title:'',
            buttons: [{
                    text: "Ok",
                    click: function() {
                        $(this).dialog("close");
                    }
                }]
        });
    }
    $("#message_dialog_"+this.id).html("<div style='height: 6em; width:350px; display: table-cell; vertical-align: middle; text-align:center;'>"+dictionary['message.text.'+this.id]+"</div>");
}

