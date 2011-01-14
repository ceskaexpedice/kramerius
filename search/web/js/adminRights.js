
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
    	$("#adminRightsWindow").dialog('option','title','Prava objektu uuid:'+_lastWorkingUuid);
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



/**
 * Dialog pro vytvoreni noveho prava
 */
var _newRight = null;
var _rightData=null;
function newRight(uuid, action) {
	var saveUrl="rights?action=create&securedaction="+action;
    var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
    var jsDataUrl = "rights?action=newrightjsdata&uuid="+uuid+"&securedaction="+action;
    $.get(fetchUrl, function(data) {
    	rightDialog(saveUrl);
        $("#newRight").html(data);
        $.get(jsDataUrl, function(data) {
        	_rightData = eval("("+data+")");
        	_rightData.init();
        });

    	$("#newRight").dialog('option','title','Nove pravo uuid:'+_lastWorkingUuid);
    });
}

/**
 * Editace existujiciho prava
 */
function editRight(uuid, rightId, action) {
	var saveUrl="rights?action=edit";
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
	var fetchUrl = "rights?action=newright&uuid="+uuid+"&securedaction="+action;
    $.get(fetchUrl, function(data) {
    	rightDialog(saveUrl);
        $("#newRight").html(data);
        $.get(jsDataUrl, function(data) {
        	_rightData = eval("("+data+")");
        	_rightData.init();
        	
        	
    		$('#uuid').attr('disabled', true);
    		$('#criterium option[value="'+_rightData.initvalues.criterium+'"]').attr("selected","selected");
    		if (_rightData.initvalues.rightCriteriumParamId==="-1") {
    			$('#checkParams').attr('checked', false);
    			$('#params option[value="new"]').attr("selected","true");
        		$("#paramsVals").val('');
        		$("#shortDesc").val('');
        		$("#longDesc").val('');
    		} else {
        		$('#checkParams').attr('checked',true);
        		$('#params option[value="'+_rightData.initvalues.rightCriteriumParamId+'"]').attr("selected","true");
        		$("#paramsVals").val(_rightData.initvalues.rightCriteriumParamVals);
        		$("#shortDesc").val(_rightData.initvalues.rightCriteriumParamShortDesc);
        		$("#longDesc").val(_rightData.initvalues.rightCriteriumParamLongDesc);
    		}
    		
    	    $("#checkParams").each(function(i,val) {
    	        callbackCriteriumParamsCheckboxChanged(val);
    	    });
    	    
    	    if (_rightData.initvalues.rightCriteriumParamId==="user") {
    			$('#userType').attr('checked', true);
    			$('#groupType').attr('checked', false);
    	    } else {
    			$('#userType').attr('checked', false);
    			$('#groupType').attr('checked', true);
    	    }
    	    
    	    $("#userId").val(_rightData.initvalues.user);
    	    if (_rightData.initvalues.fixedPriority==="0") {
    	    	$("#priority").val('');
    	    } else {
    	    	$("#priority").val(_rightData.initvalues.fixedPriority);
    	    }
    	    
        	$("#newRight").dialog('option','title','Zmena prava pro uuid:'+_lastWorkingUuid );        

        });
    });
}
/**
 * Smaze existujici pravo
 * @param uuid
 * @param rightId
 */
function deleteRight(uuid, rightId, action) {
	_saveUrlForPost="rights?action=delete";
    var jsDataUrl = "rights?action=editrightjsdata&uuid="+uuid+"&rightid="+rightId+"&securedaction="+action;
    $.get(jsDataUrl, function(data) {
    	_rightData = eval("("+data+")");
    	_rightData.init();
    	saveChangesOnlyIds();
    });
}

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
/**
 * save state and make changes; saving only ids(for delete action) 
 */
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
/**
 * save state and make chanes; saving full form (actions create and edit)
 */
function saveChanges() {
	$.post(_saveUrlForPost, {
			rightId:_rightData.initvalues.rightId,
			rightCriteriumId:_rightData.initvalues.rightCriteriumId,
			
			rightCriteriumParamId:$("#params option:selected").val()==="new" ? "-1":$("#params option:selected").val(),
			formalActionHidden:_lastDisplayedAction,
			
			uuidHidden: $("#uuid").val(),
			criteriumHidden:$("#criterium").val(),
			paramsAssocatedHidden:$("#checkParams").is(':checked') ? "true": "false",
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


/**
 * volano pri zmene stavu checkboxu
 */
function callbackCriteriumParamsCheckboxChanged(target) {
	if (target.checked) {
		$("#rightParamsCreation").show(200);
		
		$('#rightParamsSelection :select').attr('disabled', false);
        $('#rightParamsCreation :textarea').attr('disabled', false);
   } else {
		$("#rightParamsCreation").hide(200);

		$('#rightParamsSelection :select').attr('disabled', true);
        $('#rightParamsCreation :textarea').attr('disabled', true);
        $('#rightParamsCreation :textarea').val('');
   }
}

/**
 * volano pri zmene typu parametru kriteria
 */
function callbackCriteriumParamsValueChanged(target) {
    var selectedValue = target.options[target.selectedIndex].value;
//    if (selectedValue !=="new") {
//         $('#rightParamsCreation :textarea').attr('disabled', true);
//    } else {
//         $('#rightParamsCreation :textarea').attr('disabled', false);
//    }
    
    
    // vymazani textu
    if ((selectedValue!=="new") && (selectedValue!=="none")){
        $('#paramsVals').val(_rightData.params[selectedValue].values);
        $('#shortDesc').val(_rightData.params[selectedValue].shortDesc);
        $('#longDesc').val(_rightData.params[selectedValue].longDesc);
    } else {
        $('#paramsVals').val('');
        $('#paramsVals').val('');
        $('#shortDesc').val('');
        $('#longDesc').val('');
    }
}



/**
 * volano pri zmene  kriteria
 */
function callbackCriteriumValueChanged(target) {
	var selectedValue = target.options[target.selectedIndex].value;
    var needParam = _rightData.needParamsMap[selectedValue];
    if (needParam ==="false") {
    	$("#checkParams").attr('checked', false);
    } else {
    	$("#checkParams").attr('checked', true);
    }
    $("#checkParams").each(function(i,val) {
        callbackCriteriumParamsCheckboxChanged(val);
    });
}


function userAutocomplete(userTextField, lookupField, key, queryField) {
	alert("Volano jest..." );
}


/** .. pravo pro globalni akce.. */
var _globalActionsDialog;
function rightsForRepository(actions) {
	if (_globalActionsDialog) {
		_globalActionsDialog.dialog('open');
    } else {
    	$(document.body).append('<div id="globalActions">'+'</div>');
    	_globalActionsDialog = $('#globalActions').dialog({
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
	var url = "rights?action=showglobalrights&uuid=1";
    $.get(url, function(data) {
    	$("#globalActions").html(data);
    });
}
