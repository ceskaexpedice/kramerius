/**
 * @fileoverview <h3>Functions for rights administration</h3>
 */



/** Object for changing password */
function ChangePswd() {

	/** 
	 * changepswd dialog variable
	 * @private
	 */
	this.dialog = null;
}

/** Change pswd dialog */
ChangePswd.prototype.changePassword  = function () {
	var urlForPost = "users?action=savenewpswd";
	var url = "users?action=changepswd";
    $.get(url, function(data) {
    	if (this.dialog) {
    		this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="changePswd">'+'</div>');
            this.dialog = $('#changePswd').dialog({
                width:400,
                height:250,
                modal:true,
                title:"",
                buttons: {
                    "Zmen heslo": function() {
                    	if ($("#pswd").val() == $("#pswdRepeat").val()) {
                        	$.post(urlForPost, {
                        		nswpd:$("#pswd").val()},
                        		function (data,textStatus) {
                        			if (textStatus =="success") {
                        				$("#checkPswdStatus").text('Heslo zmeneno');
                        				$("#checkPswdStatus").css('color','black');

                        				$(this).dialog("close"); 
                                    	$("#changePswd").remove();
                                    	
                        			} else {
                        				$("#checkPswdStatus").css('color','red');
                        				$("#checkPswdStatus").text(dictionary['rights.changepswd.nochangepswd']);
                        			}
                        		}
                    		);


                    	} else {
            				$("#checkPswdStatus").css('color','red');
                    		$("#checkPswdStatus").text(dictionary['rights.changepswd.notsamepswd']);
                    	}
                    }, 
                    "Close": function() {
                    	$(this).dialog("close"); 
                    	$("#changePswd").remove();
                    } 
                } 
            });
        }
    	$("#changePswd").html(data);
    	$("#changePswd").dialog('option','title',dictionary['rights.changepswd.title']);
    });
}



/** MAnages all rights for one concrete action */
function AdminRights() {

	/** 
	 * Rights dialog
	 * @private
	 */
	this.dialog = null;
}



/**
 * Shows rights admin window - invoked from context menu
 * @param {int} Current displaying level
 * @param {string} model Current displaying model
 * @param {string} action Current displaying action
 */
AdminRights.prototype.adminRights = function (level, model, action) {
	hideAdminOptions(level);
	var uuid = $("#tabs_"+level).attr('pid');
	this.adminRightsImpl(uuid,action);
}

/** 
 * Shows rights admin window - can be invoked from everywhere
 * @param {string} uuid of editing object
 * @param {string} editing action
 */
AdminRights.prototype.adminRightsImpl = function (uuid,action) {
	// unbind arrows 
	keyboardSupportObject.unbindArrows();
	

	var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action;
	$.get(url, bind(function(data) {
    	if (this.dialog) {
        	this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="adminRightsWindow">'+'</div>');
            this.dialog = $('#adminRightsWindow').dialog({
                width:800,
                height:450,
                modal:true,
                title:"",
                buttons: {
                    "Close": function() {
                    	$(this).dialog("close"); 
                    	$("#rightsTableContent").remove();
                    } 
                } 
            });
            $("#adminRightsWindow").bind( "dialogclose", function(event, ui) { 
            	keyboardSupportObject.bindArrows();
            });
        }
    	
    	$("#adminRightsWindow").dialog('option','title',dictionary['rights.dialog.showrights.title']);
    	$("#adminRightsWindow").html(data);
    },this));
}


/**
 * Refreshing rights table
 * @param {string} uuid of editing object 
 * @param {action} editing action
 */
AdminRights.prototype.refreshRightsData =  function (uuid, action) {
	if (!uuid && !action) { 
		uuid = rightObject.uuid;
		action = rightObject.action; 
	}
	var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action;
    $.get(url, function(data) {
    	$("#adminRightsWindow").html(data);
    });
}

function RightDialog() {
	this.dialog = null;
}

/** Object manages one concrete right */
function Right(uuid,action,rightId) {
	this.uuid = uuid;
	this.action=action;
	this.rightId = (rightId ? rightId : -1);
	this.data=null;

}


/**
 * Shows / edit one right dialog
 * @private
 */
Right.prototype.rightDialog = function (saveUrl) {
	_saveUrlForPost = saveUrl;

    if (rightDialogObject.dialog) {
    	rightDialogObject.dialog.dialog('open');
    } else {
	if ($("#newRight").size() == 0 ) {
	    	$(document.body).append('<div id="newRight">'+'</div>');
	}
    	rightDialogObject.dialog = $('#newRight').dialog({
            width:800,
            height:450,
            modal:true,
            title:"",
            buttons: {
                "Zmen pravo": function() {
                    $(this).dialog("close"); 
                    saveChanges();
                }, 

                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
}



/**
 * Shows new right dialog
 * @param {string} uuid of editing object
 * @param {action} action editing action
 * @param {boolean} canhandlecommongroup if can handle commong group
 */
function newRight(uuid, action, canhandlecommongroup) {
    var saveUrl="rights?action=create&securedaction="+action;
    var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
    var jsDataUrl = "rights?action=newrightjsdata&uuid="+uuid+"&securedaction="+action;

    rightObject = new Right(uuid,action);

    $.get(fetchUrl, function(htmldata) {
        $.get(jsDataUrl, function(data) {
        	rightObject.rightDialog(saveUrl);
            $("#newRight").html(htmldata);
        	rightObject.data = eval("("+data+")");
        	rightObject.data.init();
        	
            if (!canhandlecommongroup) {
            	$("#allTypeSpan").hide();
            	$("#userType").attr("checked",true);

    			$("#userIdDiv").show();
    			$("#userId").val("");
    			$("#navigationForGroup").hide();
    			$("#navigationForUser").show();

            }
            $("#newRight").dialog('option','title',dictionary['rights.dialog.newright.title']);
        });
    });

}

/**
 * Shows edit right dialog 
 * @param {string} uuid of editing object
 * @param {int} rightId Id of editing right
 * @param {string} action action of editing right
 * @param {boolean} canhandlecommongroup if can handle common group
 */
function editRight(uuid, rightId, action,canhandlecommongroup) {
	var saveUrl="rights?action=edit";
        var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
	var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
	
	rightObject = new Right(uuid,action,rightId);	

        // ziskani html
	$.get(fetchUrl, function(htmldata) {
		//ziskani skritpu
		$.get(jsDataUrl, function(data) {
			rightObject.rightDialog(saveUrl);
        	$("#newRight").html(htmldata);

        	rightObject.data = eval("("+data+")");
        	rightObject.data.init();

    		$('#uuid').attr('disabled', true);
    		$('#criterium option[value="'+rightObject.data.initvalues.criterium+'"]').attr("selected","selected");

    	    $("#criterium").each(function(i,val) {
    	    	callbacks.callbackCriteriumValueChanged(val, function() {
									            		if (rightObject.data.initvalues.rightCriteriumParamId==="-1") {
									            			$('#params option[value="new"]').attr("selected","true");
									                		$("#paramsVals").val('');
									                		$("#shortDesc").val('');
									                		$("#longDesc").val('');
									            		} else {
									            			$("#params").val(""+rightObject.data.initvalues.rightCriteriumParamId);
									            			
									                		$("#paramsVals").val(rightObject.data.initvalues.rightCriteriumParamVals);
									                		$("#shortDesc").val(rightObject.data.initvalues.rightCriteriumParamShortDesc);
									                		$("#longDesc").val(rightObject.data.initvalues.rightCriteriumParamLongDesc);
									            		}
									        	    });
    	    });
    		
    	    // zakaze moznost prirazeni skupine common_users
    	    if (!canhandlecommongroup) {
            	$("#allTypeSpan").hide();
            }

    	    // inicializace zvoleneho uzivatele  ... 
    	    if (rightObject.data.initvalues.userType==="user") {
    			$('#userType').attr('checked', true);
    			$('#groupType').attr('checked', false);
    			$('#allType').attr('checked', false);
    			$("#userIdDiv").show();
    			
    			$("#navigationForGroup").hide();
    			$("#navigationForUser").show();
    			
    			
    	    } else if (rightObject.data.initvalues.user==="common_users"){
    			$('#userType').attr('checked', false);
    			$('#groupType').attr('checked', false);
    			$('#allType').attr('checked', true);
    			$("#userIdDiv").hide();

    			$("#navigationForGroup").hide();
    			$("#navigationForUser").hide();

    	    } else {
    			$('#userType').attr('checked', false);
    			$('#groupType').attr('checked', true);
    			$('#allType').attr('checked', false);
    			$("#userIdDiv").show();
    			
    			$("#navigationForGroup").show();
    			$("#navigationForUser").hide();

    	    }

    	    $("#userId").val(rightObject.data.initvalues.user);
    	    // ~ konec inicializace uzivatele
    	    
    	    if (rightObject.data.initvalues.fixedPriority==="0") {
    	    	$("#priority").val('');
    	    } else {
    	    	$("#priority").val(rightObject.data.initvalues.fixedPriority);
    	    }
    	    
    	    // TODO: lepsi titulek
        	$("#newRight").dialog('option','title',dictionary['rights.dialog.editright.title']);

        });
    });
}


/**
 * Delete existing right
 * @param {string} uuid of object 
 * @param {int} rightId Id of deleting right
 * @param {string} action action of deleting right  
 */
function deleteRight(uuid, rightId, action) {
	rightObject = new Right(uuid,action,rightId);	
	_saveUrlForPost="rights?action=delete";
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
    $.get(jsDataUrl, function(data) {
    	rightObject.data = eval("("+data+")");
    	rightObject.data.init();
    	saveChangesOnlyIds();
    });
}





//TODO Change it!!
/**
 * State property for saving right
 * @private
 */
var _saveUrlForPost=null; 
/** save state and make changes; saving only ids(for delete action)  
 * @private
 */
function saveChangesOnlyIds() {
	$.post(_saveUrlForPost, {
		rightId:rightObject.data.initvalues.rightId,
		rightCriteriumId:rightObject.data.initvalues.rightCriteriumId,
		rightCriteriumParamId:rightObject.data.initvalues.rightCriteriumParamId,
		formalActionHidden:"read"},
		function (data) {
			setTimeout("refreshRightsData();",500);
		});
}
/** save state and make chanes; saving full form (actions create and edit) 
 * @private
 */
function saveChanges() {
	$.post(_saveUrlForPost, {
			// id objektu
			rightId:rightObject.data.initvalues.rightId,
			rightCriteriumId:rightObject.data.initvalues.rightCriteriumId,
			rightCriteriumParamId:$("#params option:selected").val()==="new" ? "-1":$("#params option:selected").val(),
			// akce ktera se edituje		
			formalActionHidden:rightObject.action,
			// objekt ktereho se to tyka
			uuidHidden: $("#uuid").val(),
			
			// uzivatelem zvolene kriterium
			criteriumHidden:$("#criterium").val(),
			// uzivatelem zmenene parametry
			paramsHidden:$("#paramsVals").val(),
			paramsShortDescriptionHidden:$("#shortDesc").val(),
			paramsLongDescriptionHidden:$("#longDesc").val(),
			
			userTypeHidden:$("#userType").is(':checked') ? "user":"group",
		    userIdHidden:$("#userId").val(),
		    priorityHidden:$("#priority").val()
	}, 
	function (data) {
		setTimeout("refreshRightsData();",500);
	});
}


function Callbacks() {
}

Callbacks.prototype.typeoflist = function() {
	if ($('#userTypeList').attr('checked')) {
		return "user"
	} else if ($('#groupTypeList').attr('checked')) {
		return "group"
	} else if ($('#allTypeList').attr('checked')) {
		return "all"
	} return null;
}

/*calback*/
Callbacks.prototype.callbackUserSelectCombo = function(target, uuid, action, afterCallback) {
	if (!uuid && !action) { 
		uuid = rightObject.uuid;
		action = rightObject.action; 
	}

	var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist="+this.typeoflist();
	var selectedValue = target.options[target.selectedIndex].value;
	if (selectedValue) {
		url = url+ "&requesteduser="+selectedValue;
	}

	$.get(url, function(data) {
		$("#adminRightsWindow").html(data);
	});

}

Callbacks.prototype.callbackGroupTypeOfListValueChanged=function(target, uuid, action, afterCallback) {
	if (!uuid && !action) { 
		uuid = rightObject.uuid;
		action = rightObject.action; 
	}

	if ($('#groupTypeList').attr('checked')) {
		var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist=group";
	    $.get(url, function(data) {
	    	$("#adminRightsWindow").html(data);
	    });
	}
}

Callbacks.prototype.callbackAllTypeOfListValueChanged=function(target,uuid, action, afterCallback) {
	if (!uuid && !action) { 
		uuid = rightObject.uuid;
		action = rightObject.action; 
	}

	if ($('#allTypeList').attr('checked')) {
		var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist=all";
	    $.get(url, function(data) {
	    	$("#adminRightsWindow").html(data);
	    });
	}

}
Callbacks.prototype.callbackRadioButtonGroupValueChanged=function(target) {
	if ($('#groupType').attr('checked')) {
        $("#userautocomplete").hide();
        $("#userId").val('');
		typeOfRequest = "group";
        $("#userIdDiv").show();

		$("#navigationForGroup").show();
		$("#navigationForUser").hide();

		// skryti napovidanych hodnot
		hints.hintsAllOff();
	}
}


Callbacks.prototype.callbackRadioButtonAllValueChanged = function (target,afterCallback) {
	if ($('#allType').attr('checked')) {
        $("#userautocomplete").hide();
        $("#userId").val('common_users');
		typeOfRequest = "group";
        $("#userIdDiv").hide();
        
		$("#navigationForGroup").hide(500);
		$("#navigationForUser").hide(500,afterCallback);

		// skryti napovidanych hodnot
		hints.hintsAllOff();
	}
}

/** volano pri zmene typu parametru kriteria */
Callbacks.prototype.callbackCriteriumParamsValueChanged=function(target, afterCallback) {
	if (target.selectedIndex < 0) return;
	var selectedValue = target.options[target.selectedIndex].value;
    
	// vymazani textu
    if ((selectedValue!=="new") && (selectedValue!=="none")){
        $('#paramsVals').val(rightObject.data.params[selectedValue].values);
        $('#shortDesc').val(rightObject.data.params[selectedValue].shortDesc);
        $('#longDesc').val(rightObject.data.params[selectedValue].longDesc);
		
    } else {
		$("#rightParamsCreation").show(500, afterCallback);
		
        $('#paramsVals').val('');
        $('#shortDesc').val('');
        $('#longDesc').val('');
    }
}


/** calback for change criterium */
Callbacks.prototype.callbackCriteriumValueChanged=function(target, afterCallback) {
	var selectedCriterium = $("#criterium").val();
	var needParam = rightObject.data.needParamsMap[selectedCriterium];
	
	if (needParam==="true") {
    	$("#rightParamsCreation").show(500, afterCallback);
	} else {
		$("#rightParamsCreation").hide(500, afterCallback);
        $('#paramsVals').val('');
        $('#shortDesc').val('');
	}
	$("#params option[value='new']").attr('selected', 'selected');
}





/** zobrazeni akci ktere je mozno spravovat */
function SecuredActions() {
	this.dialog = null;
	
}

SecuredActions.prototype.securedActionsTable =  function (uuid,actions) {
	if (this.dialog) {
		this.dialog.dialog('open');
    } else {
    	$(document.body).append('<div id="globalActions">'+'</div>');
    	this.dialog = $('#globalActions').dialog({
            width:800,
            height:450,
            modal:true,
            title:"",
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
	var url = "rights?action=showglobalrights&uuid="+uuid;
	if (actions) {
		url = url+"&actions="+actions;
	}
	$.get(url, function(data) {
    	$("#globalActions").html(data);
    });
}

/** secured actions dialog volany z kontextoveho menu */
SecuredActions.prototype.securedActionsTableForCtxMenu = function(level, uuid, actions) {
	hideAdminOptions(level);
	var nuuid = $("#tabs_"+level).attr('pid');
	this.securedActionsTable(nuuid, actions);
}


/** Hints object */
function Hints() {
}

Hints.prototype.hintGroupsForUser = function() {
	this.hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
		$("#usergroupsdropdownicon").attr('src','img/dropup.png');
		var url = "users?action=hintgroupforuser&user="+$("#userId").val();
		$.get(url, function(data) {
	    	$("#hintContent").html(data);
	    });
	}
}


/** zobrazeni napovedy pro user id */
Hints.prototype.hintAutocomplete= function (target) {
	if ($('#hintContent').hasClass('down')) {
		/*
		if ($('#groupType').attr('checked')) {
			this.hintAllGroups();
		} else if ($('#userType').attr('checked')) {
			this.hintAllUsers();
		}*/


	} else {
		if ($('#groupType').attr('checked')) {
	    	$("#groupdropdownicon").attr('src','img/dropup.png');
	    	$("#userdropdownicon").attr('src','img/dropdown.png');

	    	var url = "users?action=hintallgroups&prefix="+$("#userId").val();
	    	$.get(url, function(data) {
		    	$("#hintContent").html(data);
		    });

		} else if ($('#userType').attr('checked')) {
	    	$("#groupdropdownicon").attr('src','img/dropdown.png');
	    	$("#userdropdownicon").attr('src','img/dropup.png');

	    	var url = "users?action=hintallusers&prefix="+$("#userId").val();
	    	$.get(url, function(data) {
		    	$("#hintContent").html(data);
		    });

		}

		
	}
}


/** Hide all hits */
Hints.prototype.hintsAllOff = function() {
	$('#hintContent').removeClass('up').addClass('down');
	$("#userdropdownicon").attr('src','img/dropdown.png')
	$("#groupdropdownicon").attr('src','img/dropdown.png')
	$("#usersgroupdropdownicon").attr('src','img/dropdown.png')
	$('#hintContent').removeClass('up').addClass('down');
	$("#hintContent").html("");
}

Hints.prototype.switchOneOn = function(oneOn) {
	var addresses = ["#groupdropdownicon",
	                 "#userdropdownicon",
	                 "#usersgroupdropdownicon",
	                 "#groupusersdropdownicon"];
	for(var addr in addresses) {
		if (addr == oneOn) {
			$(oneOn).attr('src','img/dropdown.png')
		} else {
			$(oneOn).attr('src','img/dropup.png')
			
		}
	}

}

/** Hint all groups */
Hints.prototype.hintAllGroups=function() {
	this.hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
    	$('#hintContent').removeClass('down').addClass('up');

	var url = "users?action=hintallgroups";
	    $.get(url, bind(function(data) {
	    	this.switchOneOn("#groupdropdownicon");
	    	$("#hintContent").html(data);
	    }, this));

	}
}

/** vyber polozky z napovedy */
Hints.prototype.hintSelect = function(loginname) {
	$("#userId").val(loginname);
	this.hintsAllOff();
}



// change password
var pswdObject = new ChangePswd();

// rights
var rightsObject = new AdminRights();

// secured actions
var securedActionsObject = new SecuredActions();


var rightObject = null;

//because of jquery 
var rightDialogObject = new RightDialog();

// hints 
var hints = new Hints();

// callbacks in components
var callbacks = new Callbacks();

