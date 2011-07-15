/**
 * @fileoverview <h3>Functions for rights administration</h3>
 */


function YesNoDialog() {
	this.dialog = null;
	this._internalfunc;
}

YesNoDialog.prototype.perform=function(msg, func) {
	this._internalfunc=func;
    	if (this.dialog) {
    		this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="yesNoDialog"><div id="yesNoDialogMsg"></div>'+'</div>');
		this.dialog = $('#yesNoDialog').dialog({
			width:300,
			height:80,
			modal:true,
			title:"",
                	buttons: {
				"Ano": bind(function() {
					this._internalfunc("yes");
	        	            	$(this.dialog).dialog("close"); 
				},this), 
				"Ne": bind(function() {
					this._internalfunc("no");
                	    		$(this).dialog("close"); 
				},this) 
			}
		});

	}

	$("#yesNoDialogMsg").html(msg);
}

var yesnodialog =  new YesNoDialog();

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
	 * Uuid of object
	 */
	this.uuid = null;
	
	/**
	 * Displaying action
	 */
	this.action = null;

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
	this.uuid = uuid;
	this.action = action;

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
AdminRights.prototype.refreshRightsData =  function () {
/*
	if (!uuid && !action) { 
		uuid = rightsObject.uuid;
		action = rightsObject.action; 
	}
*/
	var url = "rights?action=showrights&uuid="+this.uuid+"&securedaction="+this.action;
	$.get(url, function(data) {
		$("#adminRightsWindow").html(data);
	});
}


/**
 * Shows new right dialog
 * @param {string} uuid of editing object
 * @param {action} action editing action
 * @param {boolean} canhandlecommongroup if can handle commong group
 */
AdminRights.prototype.newRight=function(canhandlecommongroup) {
    var saveUrl="rights?action=create&securedaction="+this.action;
    var fetchUrl = "rights?action=newright&uuid="+this.uuid+"&securedaction="+this.action;
    var jsDataUrl = "rights?action=newrightjsdata&uuid="+this.uuid+"&securedaction="+this.action;

    rightObject = new Right(this.uuid,this.action,-1,saveUrl);

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
AdminRights.prototype.editRight=function (rightId,canhandlecommongroup) {
	var saveUrl="rights?action=edit";
        var jsDataUrl = "rights?action=editrightjsdata&uuid="+this.uuid+"&rightid="+rightId+"&securedaction="+this.action;
	var fetchUrl = "rights?action=newright&uuid="+this.uuid+"&securedaction="+this.action;
	
	rightObject = new Right(this.uuid,this.action,rightId, saveUrl);	

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
AdminRights.prototype.deleteRight=function(rightId) {
	_saveUrlForPost="rights?action=delete";
	rightObject = new Right(this.uuid,this.action,rightId,_saveUrlForPost);	
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+this.uuid+"&rightid="+rightId+"&securedaction="+this.action;
    $.get(jsDataUrl, bind(function(data) {
    	rightObject.data = eval("("+data+")");
    	rightObject.data.init();
    	rightObject.saveChangesOnlyIds();
    },this));
}


function RightDialog() {
	this.dialog = null;
}

/** Object manages one concrete right */
function Right(uuid,action,rightId, saveUrl) {
	this.uuid = uuid;
	this.action=action;
	this.saveUrl=saveUrl;
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
                    rightObject.saveChanges();
                }, 

                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
}


/** save state and make changes; saving only ids(for delete action)  
 * @private
 */
Right.prototype.saveChangesOnlyIds=function() {
	$.post(this.saveUrl, {
		rightId:rightObject.data.initvalues.rightId,
		rightCriteriumId:rightObject.data.initvalues.rightCriteriumId,
		rightCriteriumParamId:rightObject.data.initvalues.rightCriteriumParamId,
		formalActionHidden:"read"},
		function (data) {
			setTimeout("rightsObject.refreshRightsData();",500);
		});
}







/** save state and make chanes; saving full form (actions create and edit) 
 * @private
 */
Right.prototype.saveChanges=function() {
	$.post(this.saveUrl, {
			// id objektu
			rightId:rightObject.data.initvalues.rightId,
			rightCriteriumId:rightObject.data.initvalues.rightCriteriumId,
			rightCriteriumParamId:$("#params option:selected").val()==="new" ? "-1":$("#params option:selected").val(),
			// akce ktera se edituje		
			formalActionHidden:rightsObject.action,
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
		setTimeout("rightsObject.refreshRightsData();",500);
	});
}


function Callbacks() {}

Callbacks.prototype.typeoflist = function() {
	if ($('#userTypeList').attr('checked')) {
		return "user";
	} else if ($('#groupTypeList').attr('checked')) {
		return "group";
	} else if ($('#allTypeList').attr('checked')) {
		return "all";
	} else return null;
}


/*calback*/
Callbacks.prototype.callbackUserSelectCombo = function(target, uuid, action, afterCallback) {
	if (!uuid && !action) { 
		uuid = rightsObject.uuid;
		action = rightsObject.action; 
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
		uuid = rightsObject.uuid;
		action = rightsObject.action; 
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
		uuid = rightsObject.uuid;
		action = rightsObject.action; 
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



function Roles() {
	this.dialog = null;
	this.roleDialog = null;
}

/**
 * Check role name 
 * 
 * @param {object} html input
 */
Roles.prototype.rolesCheck=function(inp) {
	var forbiddenVals = mapJQuerySelector(function(obj) {
		return $(obj).text(); 
	},$("#allRolesId option")); 	
	
	var reducedValue = reduce(function(baseVal,iteratingVal) { 
		return (baseVal === iteratingVal) ? "" : baseVal;
	}, $(inp).val(), forbiddenVals);

	if(reducedValue === "") {
		$("#roleNameError").css('visibility','visible');	
		return false;
	} else {
		$("#roleNameError").css('visibility','hidden');	
		return true;
	}

}

/**
 * Rerfreshing roles table 
 * 
 * @param {object} html input
 */
Roles.prototype.refreshRoles=function() {
	var url = "users?action=showroles";
	$.get(url, function(data) {
    		$("#roles").html(data);
    	});
}

Roles.prototype.showRoles=function() {
	if (this.dialog) {
		this.dialog.dialog('open');
	} else {
		$(document.body).append('<div id="roles">'+'</div>');
		this.dialog = $('#roles').dialog({
		    width:600,
		    height:400,
		    modal:true,
		    title:"",
		    buttons: {
			"Close": bind(function() {
			    $(this.dialog).dialog("close"); 
			},this) 
		    } 
		});
	}
	var url = "users?action=showroles";
	$.get(url, function(data) {
    		$("#roles").html(data);
    	});

}


Roles.prototype.dialogForRole = function(savefunction) {
	this._savefunction = savefunction;
    	if (this.roleDialog) {
    		this.roleDialog.dialog('open');
    	} else {
    		$(document.body).append('<div id="role">'+'</div>');
    		this.roleDialog = $('#role').dialog({
    		    width:600,
    		    height:400,
    		    modal:true,
    		    title:"",
    		    buttons: {
			"Close": bind(function() {
			    $(this.roleDialog).dialog("close"); 
			     this.refreshRoles();	
			},this),
			"Save": bind(function() {
				if (this.rolesCheck($("#name").get(0))) {
					this._savefunction();	
				}	
				$(this.roleDialog).dialog("close"); 
	    		        this.refreshRoles();	
			},this)
		    }	
    		});
    	}
}





Roles.prototype.newRole = function() {
    var url = "users?action=newrole";
    $.get(url, bind(function(data) {
	this.dialogForRole(
	function() {
		var strct = {
			id:$("#roleId").val(),
			name:$("#name").val(),
			personalAdminId:$("#personalAdminId").val()
		};

	 	$.post("users?action=newrole", strct,function() {})
			/* JQUERY 1.5
			.error(
				function() { }
			)*/;

		});
    	// dialog content
    	$('#role').html(data);
    }, this));
}

Roles.prototype.editRole = function(rolename) {

    var url = "users?action=editrole&rolename="+rolename;
    $.get(url, bind(function(data) {
	this.dialogForRole(function() {

		var strct = {
			id:$("#roleId").val(),
			name:$("#name").val(),
			personalAdminId:$("#personalAdminId").val()
		};

	 	$.post("users?action=saverole", strct,function() {})
		/* JQUERY 1.5 
		.error(function() { 
			//alert(arguments[0].status);
		})*/;

		
	});
    	// dialog content
    	$('#role').html(data);
    }, this));
}


Roles.prototype.deleteRole = function(rolename) {

	var strct = {
		name:rolename,
	};

	yesnodialog.perform("<table width='100%' height='100%'><tr height='100%'><td align='center' valign='center'><strong> Smazat vybranou roli? </strong></td></tr></div>",bind(function(arg) {
		if (arg === "yes") {
		 	$.post("users?action=deleterole", strct,function() {})
			/* JQUERY 1.5			
			.error(function() { 
				alert(arguments[0].status);
			})*/;
		}
		this.refreshRoles();
	},this));
}


/**
 * Change pswd object
 */
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

var roles = new Roles();


