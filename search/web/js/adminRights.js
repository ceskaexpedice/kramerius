
/** dialog pro zmenu hesla */
var _changePswdDialog;
function changePassword() {
	var urlForPost = "users?action=savenewpswd";

	var url = "users?action=changepswd";
    $.get(url, function(data) {
    	if (_changePswdDialog) {
    		_changePswdDialog.dialog('open');
        } else {
            $(document.body).append('<div id="changePswd">'+'</div>');
            _changePswdDialog = $('#changePswd').dialog({
                width:400,
                height:250,
                modal:true,
                title:"Zmena hesla",
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
                        				$("#checkPswdStatus").text('Heslo se nepodarilo zmenit');
                        			}
                        		}
                    		);


                    	} else {
            				$("#checkPswdStatus").css('color','red');
                    		$("#checkPswdStatus").text('Zadana hesla nesedi !');
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

    });
	
}

/** zobrazi administracni dialog prav. - volano ze stranky, kde je pritomny level a model (zbytek si funkce najde) */
var _rightsWindow = null;
var _lastWorkingUuid=null;
var _lastDisplayedAction = null;
function adminRights(level, model, action) {
	hideAdminOptions(level);
	var uuid = $("#tabs_"+level).attr('pid');
	adminRightsImpl(uuid,action);
}

/** -"-   - volano odkudkoliv */
function adminRightsImpl(uuid,action) {
	_lastWorkingUuid = uuid;
	_lastDisplayedAction = action;

	var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action;
    $.get(url, function(data) {
    	if (_rightsWindow) {
        	_rightsWindow.dialog('open');
        } else {
            $(document.body).append('<div id="adminRightsWindow">'+'</div>');
            _rightsWindow = $('#adminRightsWindow').dialog({
                width:800,
                height:450,
                modal:true,
                title:"Prava objektu",
                buttons: {
                    "Close": function() {
                    	_lastWorkingUuid = null;
                    	$(this).dialog("close"); 
                    	$("#rightsTableContent").remove();
                    } 
                } 
            });
        }
    	$("#adminRightsWindow").dialog('option','title','Prava objektu');
    	$("#adminRightsWindow").html(data);

    });
}


/** refresh na tabulku prav */
function refreshRightsData(uuid, action) {
	if (!uuid) { uuid = _lastWorkingUuid; }
	if (!action) {action = _lastDisplayedAction;}
	var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action;
    $.get(url, function(data) {
    	$("#adminRightsWindow").html(data);
    });
}



/** dialog pro vytvoreni noveho prava */
var _newRight = null;
var _rightData=null;
function newRight(uuid, action, canhandlecommongroup) {
	var saveUrl="rights?action=create&securedaction="+action;
    var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
    var jsDataUrl = "rights?action=newrightjsdata&uuid="+uuid+"&securedaction="+action;
    $.get(fetchUrl, function(htmldata) {
        $.get(jsDataUrl, function(data) {
        	rightDialog(saveUrl);
            $("#newRight").html(htmldata);
        	_rightData = eval("("+data+")");
        	_rightData.init();
        	
            if (!canhandlecommongroup) {
            	$("#allTypeSpan").hide();
            	$("#userType").attr("checked",true);

    			$("#userIdDiv").show();
    			$("#userId").val("");
    			$("#navigationForGroup").hide();
    			$("#navigationForUser").show();

            }

        	$("#newRight").dialog('option','title','Nove pravo');
        });

    	
    });

}

/** editace existujiciho prava */
function editRight(uuid, rightId, action,canhandlecommongroup) {
	var saveUrl="rights?action=edit";
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
	var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
	// ziskani html
	$.get(fetchUrl, function(htmldata) {
		//ziskani skritpu
		$.get(jsDataUrl, function(data) {
			rightDialog(saveUrl);
        	$("#newRight").html(htmldata);

        	_rightData = eval("("+data+")");
        	_rightData.init();

    		$('#uuid').attr('disabled', true);
    		$('#criterium option[value="'+_rightData.initvalues.criterium+'"]').attr("selected","selected");

    	    $("#criterium").each(function(i,val) {
    	    	callbackCriteriumValueChanged(val, function() {
									            		if (_rightData.initvalues.rightCriteriumParamId==="-1") {
									            			$('#params option[value="new"]').attr("selected","true");
									                		$("#paramsVals").val('');
									                		$("#shortDesc").val('');
									                		$("#longDesc").val('');
									            		} else {
									            			$("#params").val(""+_rightData.initvalues.rightCriteriumParamId);
									            			
									                		$("#paramsVals").val(_rightData.initvalues.rightCriteriumParamVals);
									                		$("#shortDesc").val(_rightData.initvalues.rightCriteriumParamShortDesc);
									                		$("#longDesc").val(_rightData.initvalues.rightCriteriumParamLongDesc);
									            		}
									        	    });
    	    });
    		
    	    // zakaze moznost prirazeni skupine common_users
    	    if (!canhandlecommongroup) {
            	$("#allTypeSpan").hide();
            }

    	    // inicializace zvoleneho uzivatele  ... 
    	    if (_rightData.initvalues.userType==="user") {
    			$('#userType').attr('checked', true);
    			$('#groupType').attr('checked', false);
    			$('#allType').attr('checked', false);
    			$("#userIdDiv").show();
    			
    			$("#navigationForGroup").hide();
    			$("#navigationForUser").show();
    			
    			
    	    } else if (_rightData.initvalues.user==="common_users"){
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

    	    $("#userId").val(_rightData.initvalues.user);
    	    // ~ konec inicializace uzivatele
    	    
    	    if (_rightData.initvalues.fixedPriority==="0") {
    	    	$("#priority").val('');
    	    } else {
    	    	$("#priority").val(_rightData.initvalues.fixedPriority);
    	    }
    	    
    	    // TODO: lepsi titulek
        	$("#newRight").dialog('option','title','Zmena prava ');        

        });
    });
}


/** smaze existujici pravo */
function deleteRight(uuid, rightId, action) {
	_saveUrlForPost="rights?action=delete";
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
    $.get(jsDataUrl, function(data) {
    	_rightData = eval("("+data+")");
    	_rightData.init();
    	saveChangesOnlyIds();
    });
}

/** zobrazeni jednoho prava */
function rightDialog(saveUrl) {
	_saveUrlForPost = saveUrl;
	if (_newRight) {
    	_newRight.dialog('open');
    } else {
    	$(document.body).append('<div id="newRight">'+'</div>');
    	_newRight = $('#newRight').dialog({
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




var _saveUrlForPost=null; 
/** save state and make changes; saving only ids(for delete action)  */
function saveChangesOnlyIds() {
	$.post(_saveUrlForPost, {
		rightId:_rightData.initvalues.rightId,
		rightCriteriumId:_rightData.initvalues.rightCriteriumId,
		rightCriteriumParamId:_rightData.initvalues.rightCriteriumParamId,
		formalActionHidden:"read"},
		function (data) {
			setTimeout("refreshRightsData();",500);
		});
}
/** save state and make chanes; saving full form (actions create and edit) */
function saveChanges() {
//	alert($("#params option:selected").val());
	$.post(_saveUrlForPost, {
			// id objektu
			rightId:_rightData.initvalues.rightId,
			rightCriteriumId:_rightData.initvalues.rightCriteriumId,
			rightCriteriumParamId:$("#params option:selected").val()==="new" ? "-1":$("#params option:selected").val(),
			// akce ktera se edituje		
			formalActionHidden:_lastDisplayedAction,
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

function typeoflist() {
	if ($('#userTypeList').attr('checked')) {
		return "user"
	} else if ($('#groupTypeList').attr('checked')) {
		return "group"
	} else if ($('#allTypeList').attr('checked')) {
		return "all"
	} return null;
}


//Callbacks from list of actions dialog
function callbackUserSelectCombo(target, uuid, action, afterCallback) {
	if (!uuid) { uuid = _lastWorkingUuid; }
	if (!action) {action = _lastDisplayedAction;}

    var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist="+typeoflist();
    var selectedValue = target.options[target.selectedIndex].value;
    if (selectedValue) {
    	url = url+ "&requesteduser="+selectedValue;
    }
    
	$.get(url, function(data) {
    	$("#adminRightsWindow").html(data);
    });

}
function callbackUserTypeOfListValueChanged(target, uuid, action, afterCallback) {

	if (!uuid) { uuid = _lastWorkingUuid; }
	if (!action) {action = _lastDisplayedAction;}

	if ($('#userTypeList').attr('checked')) {
		var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist=user";
	    $.get(url, function(data) {
	    	$("#adminRightsWindow").html(data);
	    });
	}
}

function callbackGroupTypeOfListValueChanged(target, uuid, action, afterCallback) {
	if (!uuid) { uuid = _lastWorkingUuid; }
	if (!action) {action = _lastDisplayedAction;}

	if ($('#groupTypeList').attr('checked')) {
		var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist=group";
	    $.get(url, function(data) {
	    	$("#adminRightsWindow").html(data);
	    });
	}
}

function callbackAllTypeOfListValueChanged(target,uuid, action, afterCallback) {

	if (!uuid) { uuid = _lastWorkingUuid; }
	if (!action) {action = _lastDisplayedAction;}

	if ($('#allTypeList').attr('checked')) {
		var url = "rights?action=showrights&uuid="+uuid+"&securedaction="+action+"&typeoflist=all";
	    $.get(url, function(data) {
	    	$("#adminRightsWindow").html(data);
	    });
	}
}

// Callbacks from one right edit dialog
/** volano pri zmene radiibuttonu - vybrano user */
function callbackRadioButtonUserValueChanged(target, afterCallback) {
	if ($('#userType').attr('checked')) {
        $("#userautocomplete").hide();
        $("#userId").val('');
        typeOfRequest = "user";
        $("#userIdDiv").show();

        $("#navigationForGroup").hide();
		$("#navigationForUser").show();
		
		// skryti napovidanych hodnot
		hintsAllOff();
	}
}
/** volano pri zmene  radiobuttonu - vybrano group*/
function callbackRadioButtonGroupValueChanged(target) {
	if ($('#groupType').attr('checked')) {
        $("#userautocomplete").hide();
        $("#userId").val('');
		typeOfRequest = "group";
        $("#userIdDiv").show();

		$("#navigationForGroup").show();
		$("#navigationForUser").hide();

		// skryti napovidanych hodnot
		hintsAllOff();
	}
}

/** volano pri zmene  radiobuttonu - vybrano vse*/
function callbackRadioButtonAllValueChanged(target,afterCallback) {
	if ($('#allType').attr('checked')) {
        $("#userautocomplete").hide();
        $("#userId").val('common_users');
		typeOfRequest = "group";
        $("#userIdDiv").hide();
        
		$("#navigationForGroup").hide(500);
		$("#navigationForUser").hide(500,afterCallback);

		// skryti napovidanych hodnot
		hintsAllOff();
	}
}

/** volano pri zmene typu parametru kriteria */
function callbackCriteriumParamsValueChanged(target, afterCallback) {
	if (target.selectedIndex < 0) return;
	var selectedValue = target.options[target.selectedIndex].value;
    
	// vymazani textu
    if ((selectedValue!=="new") && (selectedValue!=="none")){
        $('#paramsVals').val(_rightData.params[selectedValue].values);
        $('#shortDesc').val(_rightData.params[selectedValue].shortDesc);
        $('#longDesc').val(_rightData.params[selectedValue].longDesc);
		
    } else {
		$("#rightParamsCreation").show(500, afterCallback);
		
        $('#paramsVals').val('');
        $('#shortDesc').val('');
        $('#longDesc').val('');
    }
}


/** calback for change criterium */
function callbackCriteriumValueChanged(target, afterCallback) {
	var selectedCriterium = $("#criterium").val();
	var needParam = _rightData.needParamsMap[selectedCriterium];
	
	if (needParam==="true") {
    	$("#rightParamsCreation").show(500, afterCallback);
	} else {
		$("#rightParamsCreation").hide(500, afterCallback);
        $('#paramsVals').val('');
        $('#shortDesc').val('');
	}
	$("#params option[value='new']").attr('selected', 'selected');
}


/** autocomplete for user :TODO: Delete this */
function autocompleteResult(value, lookupField) {
	$("#userId").val(value);
}


var typeOfRequest="group";
function doUserAutocomplete(userTextField, lookupField, key, queryField) {
	autoCompleteDiv="#userautocomplete";
    completeUrl = "users?action=userjsautocomplete&autcompletetype="+typeOfRequest+"&";
    resultClickFunctionName="autocompleteResult";
    	//rights?field=user&t=common_usersaajffa
    if( key.keyCode >=16 && key.keyCode <= 19 ){
        return;
    }
    //arrows
    if( key.keyCode >=37 && key.keyCode <= 40){
        moveSelected(key.keyCode, queryField);
        return;
    }
    if( key.keyCode == 13){
    	autocompleteResult($("#userautocomplete .selected").text());
        $("#userautocomplete").hide();
    	return;
    }
    json(userTextField, lookupField, queryField);
}
/** ~end autocomplete for user */



/** zobrazeni akci ktere je mozno spravovat */
var _securedActionsDialog;
function securedActionsTable(uuid,actions) {
	if (_securedActionsDialog) {
		_securedActionsDialog.dialog('open');
    } else {
    	$(document.body).append('<div id="globalActions">'+'</div>');
    	_securedActionsDialog = $('#globalActions').dialog({
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
function securedActionsTableForCtxMenu(level, uuid, actions) {
	hideAdminOptions(level);
	var nuuid = $("#tabs_"+level).attr('pid');
	securedActionsTable(nuuid, actions);
}

/** napovi skupiny pro vybraneho uzivatele */
function hintGroupsForUser() {
	hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
		$("#usergroupsdropdownicon").attr('src','img/dropup.png');
		var url = "users?action=hintgroupforuser&user="+$("#userId").val();
		$.get(url, function(data) {
	    	$("#hintContent").html(data);
	    });
	}
}

/** zobrazeni napovedy pro user id */
function hintAutocomplete(target) {
	if ($('#hintContent').hasClass('down')) {
		if ($('#groupType').attr('checked')) {
			hintAllGroups();
		} else if ($('#userType').attr('checked')) {
			hintAllUsers();
		}
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

/** vyber polozky z napovedy */
function hintSelect(loginname) {
	$("#userId").val(loginname);
	hintsAllOff();
}


/** napoveda - zobrazi uzivatele */
function hintAllUsers() {
	hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
    	$('#hintContent').removeClass('down').addClass('up');
		var url = "users?action=hintallusers&prefix="+$("#userId").val();
	    $.get(url, function(data) {
	    	switchOneOn("#userdropdownicon");
	    	$("#hintContent").html(data);
	    });
	}
}

function switchOneOn(oneOn) {
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

/** vypnuti napovedy */
function hintsAllOff() {
	$('#hintContent').removeClass('up').addClass('down');
	$("#userdropdownicon").attr('src','img/dropdown.png')
	$("#groupdropdownicon").attr('src','img/dropdown.png')
	$("#usersgroupdropdownicon").attr('src','img/dropdown.png')
	$('#hintContent').removeClass('up').addClass('down');
	$("#hintContent").html("");
}

/** napoveda zobrazi skupiny */
function hintAllGroups() {
	hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
    	$('#hintContent').removeClass('down').addClass('up');

		var url = "users?action=hintallgroups";
	    $.get(url, function(data) {
	    	switchOneOn("#groupdropdownicon");
	    	$("#hintContent").html(data);
	    });

	}
}

/** zobrazi uzivatele vybrane skupiny */
function hintUsersForGroup() {
	hintsAllOff();
	if ($('#hintContent').hasClass('down')) {
		var url = "users?action=hintusersforgroup&group="+$("#userId").val();
		$.get(url, function(data) {
	    	switchOneOn("#groupusersdropdownicon");
			$("#hintContent").html(data);
	    });
	}
}