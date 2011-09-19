/**
 * @fileoverview <h3>Rights administration module</h3>
 */



/** input field dialog */
function InputTextDialog(options) {
	this.options = options || {title:'#ntitle',label:'',value:''};
	this.dialog = null;
}

InputTextDialog.prototype.open = function(okfunc,cancelfunc) {
	if (this.dialog ) {
		this.dialog.dialog('open');
    } else {
		var items = mapJQuerySelector(function (item) {
			return items;
		},$("#inputDialog"));

		if (items || items.length == 0) {
	        $(document.body).append('<div id="inputDialog"> <strong><label id="inputDialogLabel" forname="fortxt"></label></strong><br><input id="inputDialogTxt" style="width:100%"></input></div>');
		}
        this.dialog = $('#inputDialog').dialog({
            width:400,
            height:250,
            modal:true,
            title:'#title',
            buttons: {
                "Ok": bind(function() {
                	if (okfunc) okfunc($("#inputDialogTxt").val());
                	this.dialog.dialog("close"); 
                },this),
                "Cancel":bind(function() {
                	if(cancelfunc) cancelfunc();
                	this.dialog.dialog("close"); 
                },this)
            }
        });   
    }

	$("#inputDialogTxt").val(this.options.value);
	$("#inputDialogLabel").text(this.options.label);
	
	this.dialog.dialog("option","title",this.options.title);
	$( '#inputDialog' ).dialog( "option", "title", this.options.title );
			
}


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
	var url = "inc/admin/_change_pswd.jsp";
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


var affectedObjectsRights = new AffectedObjectsRights();

/** object that can manage rights */
function AffectedObjectsRights() {
	this.dialog = null;
	this.pids=[];

	// special tabs for context menu
	this.securedActionTabs = {};
	this.openedDetails=[];
	
	
}
/** On change -> event called from objects selection dialog
 */
AffectedObjectsRights.prototype.onChange=function(pid) {
	
	var npids = [];
	$("#rightsAffectedObject_selected input:checked").each(function(index,val) {
		var v = $(val).val();
		npids.push( {pid:v,model:''} );
	});
	this.pids = npids;
	
	this.clearTabs(function(tab) {
		return this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+tab;
	});
}

/** clears tab objects
 */
AffectedObjectsRights.prototype.clearTabs = function(/** Function */ retrieveUrlFunction) {
	for(var tab in this.securedActionTabs) {
		this.securedActionTabs[tab].dirty=true;	
		//this.securedActionTabs[tab].retrieveUrl = this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+tab;
		this.securedActionTabs[tab].retrieveUrl = bind(retrieveUrlFunction, this)(tab);
	}
}

/** opens dialog for displaying selected objects 
 */
AffectedObjectsRights.prototype.openDialog = function(/** array of struct */pids) {
	
	this.pids = pids;
	var url = this.url("inc/admin/_display_objects_dialog.jsp?pids=");

	// clear tabs with retreive url -> context menu
	/*
	this.clearTabs(function(tab) {
		return this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+tab;
	});
	*/

	$.get(url, bind(function(data){
    	if (this.dialog) {
    		this.dialog.dialog('open');
    	} else {
            $(document.body).append('<div id="affectedObjectRights"></div>')
            this.dialog = $('#affectedObjectRights').dialog({
                width:800,
                height:600,
                modal:true,
                title:"#title",
                buttons: {
                	"Editovat vyber": function() {
                        $(this).dialog("close");
                	},
                    "Close": function() {
                        $(this).dialog("close");
                    }
                }
            });
    	}
    	
    	
    	$('#affectedObjectRights').html(data);


    	$('a[href*="rightsAffectedObject"]').each(bind(function(index,elm) {
    		var action = $(elm).data('action');
    		if (action) {
        		this.securedActionTabs[action] = this.createSecurityActionTab(action,this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+action);
        		// change retrive function
        		this.securedActionTabs[action].retrieve = this.securedActionTabs[action].retrieveContextContent;
    		}
    	}, this));    

    	
    	// display tabs
    	$("#rightsAffectedObject_tabs").tabs({
    		select: bind(function(event, ui) { 
    			this.changeTab(event,ui);
    		},this)
    	});
    	
    	$("#rightsAffectedObject_tabs").tabs( "select" , 0);
    	
    },this));
}

/** creates secured action tab */
AffectedObjectsRights.prototype.createSecurityActionTab = function(/** String */ action, /** retrieve url */url) {
	var securedActionTab = new SecuredActionTab( {
		securedAction:action,
		retrieveUrl: url,
		pids:this.pids
	});
	securedActionTab.url = bind(this.url, this);
	return securedActionTab;
}

/** construct url from selected pids
 */
AffectedObjectsRights.prototype.url = function(/** String */baseUrl, /** Array */ pids) {
	if (!pids) pids = this.pids;
	return baseUrl+"{"+reduce(function(base, item, status) {
    	base = base+item.pid.replaceAll(":","\\:")+ (status.last ? "": ";");
        return base;
    }, "",pids)+"}";        
}




/** change display 
 */
AffectedObjectsRights.prototype.displayDetails = function(id) {
	
	if ($("#"+id).is(':visible')) {
		$("#"+id+"_icon").removeClass('ui-icon ui-icon-triangle-1-s folder');
		$("#"+id+"_icon").addClass('ui-icon ui-icon-triangle-1-e folder');
		var index = this.openedDetails.indexOf(id);
		if (index >= 0) {
			this.openedDetails.rm(index);
		}
	} else {
		$("#"+id+"_icon").removeClass('ui-icon ui-icon-triangle-1-e folder');
		$("#"+id+"_icon").addClass('ui-icon ui-icon-triangle-1-s folder');
		this.openedDetails.push(id);
	}
	
	$("#"+id).toggle();

	
}



/** event hook - > renders rights content 
 */
AffectedObjectsRights.prototype.changeTab = function(/** dom event */event, /** jquery object represents selected tab*/ui) {
	var action = $(ui.tab).data("action");
	if (this.securedActionTabs[action] && this.securedActionTabs[action].dirty) {
		this.securedActionTabs[action].retrieve();
	}
}



/** Secured  tab
 * @param struct
 * @returns {SecuredActionTab}
 */
function SecuredActionTab(struct) {
	this.dirty = true;

	this.pids = struct.pids;
	this.securedAction = struct.securedAction;
	this.retrieveUrl = struct.retrieveUrl;
	
	this.newRightDialog = null;

	this.globalDeleteDialog = null;
	this.globalEditDialog = null;

	/** op*/
	this.operation = CREATEOP;
} 


SecuredActionTab.prototype.globalEdit = function() {
	this.operation = EDITOP;
	var editUrl = this.url("inc/admin/_display_rights_for_edit.jsp?pids=")+"&action=edit&securedaction="+this.securedAction;
	$.get(editUrl, bind(function(data){
		if (this.globalEditDialog) {
			this.globalEditDialog.dialog('open');
		} else {
	        $(document.body).append('<div id="gDeleteDialog"></div>')
	        this.globalEditDialog = $('#gDeleteDialog').dialog({
	            width:640,
	            height:480,
	            modal:true,
	            title:"#title",
	            buttons: {
	            	"Edit": bind(function() {
	            		$("#editRights input:checked").each(bind(function(i, val) {
	            			var arr = $(val).val().split("_");
	            			this.editRightForPath(arr[0], arr[1]);
	            		},this));

	            		this.globalEditDialog.dialog("close");
	            	},this),
	                "Close": bind(function() {
	            		this.globalEditDialog.dialog("close");
	                },this)
	            }
	        });
			
		}
		$('#gDeleteDialog').html(data);		
	},this));

}

SecuredActionTab.prototype.globalDelete = function() {
	this.operation = DELETEOP;

	var deleteUrl = this.url("inc/admin/_display_rights_for_delete.jsp?pids=")+"&securedaction="+this.securedAction;
	$.get(deleteUrl, bind(function(data){
		if (this.globalDeleteDialog) {
			this.globalDeleteDialog.dialog('open');
		} else {
	        $(document.body).append('<div id="gDeleteDialog"></div>')
	        this.globalDeleteDialog = $('#gDeleteDialog').dialog({
	            width:640,
	            height:480,
	            modal:true,
	            title:"#title",
	            buttons: {
	            	"Smazat": bind(function() {
	            		var rightIds= [];
	            		$("#delRights input:checked").each(function(i, val) {
	            			rightIds.push($(val).val());
	            		});
	            		rightContainer = {deletedrights:rightIds};
        				this.post();

	            		this.globalDeleteDialog.dialog("close");
	            	},this),
	                "Close": bind(function() {
	            		this.globalDeleteDialog.dialog("close");
	                },this)
	            }
	        });
			
		}
		$('#gDeleteDialog').html(data);		
	},this));

		
	
}




/** refreshing content
 */
SecuredActionTab.prototype.retrieveContextContent = function() {
	$("#"+this.securedAction+"_waiting").html("Nacitani...");
	
	$.get(this.retrieveUrl, bind(function(data){
    	$('#rightsAffectedObject_'+this.securedAction).html(data);
    	$("#"+this.securedAction+"_waiting").html("");
	},this));

	this.dirty = false;
}

SecuredActionTab.prototype.retrieveGlobalContent = function() {
	$("#rightsForAction").html(dictionary['administrator.dialogs.waiting']);
	
	$.get(this.retrieveUrl, bind(function(data){
    	$("#rightsForAction").html(data);
	},this));

	this.dirty = false;
	
}

SecuredActionTab.prototype.newRight = function() {
	this.operation = CREATEOP;
	var url = this.url("inc/admin/_new_right.jsp?pids=")+"&action=create&securedaction="+this.securedAction;
	$.get(url, bind(function(data){
		if (this.newRightDialog) {
    		this.newRightDialog.dialog('open');
    	} else {
            $(document.body).append('<div id="nRightDialog"></div>')
            this.newRightDialog = $('#nRightDialog').dialog({
                width:640,
                height:480,
                modal:true,
                title:"#title",
                buttons: {
                	"Apply": bind(function() {
        				this.post();
                		//$.post("rights?action=create", flatten({data:rightContainer.data,affectedObjects:rightContainer.affectedObjects}));
                		this.newRightDialog.dialog("close");
                	},this),
                    "Close": bind(function() {
                		this.newRightDialog.dialog("close");
                    },this)
                }
            });
    	}
    	
    	$('#nRightDialog').html(data);
    	
	},this));
}


SecuredActionTab.prototype.post = function() {
	var struct = flatten({data:rightContainer.data,affectedObjects:rightContainer.affectedObjects, deletedrights:rightContainer.deletedrights});
	$.post("rights?action="+this.operation.name, struct, bind(function(){
		this.retrieve();
	},this));
}

SecuredActionTab.prototype.newRightForPath = function(path) {
	this.operation = CREATEOP;
	var arr = toStringArray(path);
	var url = this.url("inc/admin/_new_right.jsp?pids=",[{pid:arr[arr.length-1].trim()}])+"&action=create&securedaction="+this.securedAction;

	$.get(url, bind(function(data){
		
		if (this.newRightDialog) {
    		this.newRightDialog.dialog('open');
    	} else {
            $(document.body).append('<div id="nRightDialog"></div>')
            this.newRightDialog = $('#nRightDialog').dialog({
                width:640,
                height:480,
                modal:true,
                title:"#title",
                buttons: {
                	"Apply": bind(function() {
                				this.post();
                				this.newRightDialog.dialog("close");
                	},this),
                    "Close": bind(function() {
                		this.newRightDialog.dialog("close");
                    },this)
                }
            });
    	}
    	
    	$('#nRightDialog').html(data);
    	
	},this));
}

SecuredActionTab.prototype.editRightForPath=function(/** ident for right */rightId, path) {
	this.operation = EDITOP;
	var arr = toStringArray(path);
	var url = this.url("inc/admin/_new_right.jsp?pids=",[{pid:arr[arr.length-1].trim()}])+"&action=edit&ids="+rightId+"&securedaction="+this.securedAction;
	$.get(url, bind(function(data){
		
		if (this.newRightDialog) {
    		this.newRightDialog.dialog('open');
    	} else {
            $(document.body).append('<div id="nRightDialog"></div>')
            this.newRightDialog = $('#nRightDialog').dialog({
                width:640,
                height:480,
                modal:true,
                title:"...",
                buttons: {
                	"Apply": bind(function() {
        				this.post();
                		this.newRightDialog.dialog("close");
                	},this),
                    "Close": bind(function() {
                		this.newRightDialog.dialog("close");
                    },this)
                }
            });
    	}
		
    	
    	$('#nRightDialog').html(data);
	},this));
	

}

SecuredActionTab.prototype.deleteRightForPath=function(/** ident for right */rightId, path) {
	this.operation = DELETEOP;
	rightContainer = {deletedrights:[rightId]};
	this.post();
}


/** jedno editovane pravo */
var rightContainer = {};

var objects = {};



function toStringArray(str) {
	var nstr = str.substring(1,str.length-1);
	return nstr.split(',');
}

function flatten(struct, prefix) {
	var retval = {};
	var processing = [];
	processing.push({struct:struct, prefix:""});
	while(processing.length>0) {
		var obj = processing.pop();
		var cstruct = obj.struct;
		var cprefix = obj.prefix;
		for ( var item in cstruct) {
			var type = typeof cstruct[item];
			if (type === "object") {
				processing.push({struct:cstruct[item], prefix:cprefix+"["+item+"]"});
			} else if (type ==="array") {
				processing.push({struct:cstruct[item], prefix:cprefix+"["+item+"]"});
			} else if (type ==="function") {
				// skip 
			} else if (type ==="undefined") {
				// skip 
			} else {
				retval[cprefix+item]=cstruct[item]; 				
			}
		}
	}
	return retval;
}



/** Global actions */
function GlobalActions() {
	this.dialog = null;
	this.actionDialog = null;
}

GlobalActions.prototype.rigthsForAction=function(action) {
	// affected rights secured actions 
	affectedObjectsRights.securedActionTabs[action] = affectedObjectsRights.createSecurityActionTab(action,"inc/admin/_display_rights_for_global_actions.jsp?pids={uuid\\:1}&securedaction="+action);
	affectedObjectsRights.securedActionTabs[action].retrieve = affectedObjectsRights.securedActionTabs[action].retrieveGlobalContent;
	
	var url = "inc/admin/_display_rights_for_global_actions.jsp?pids={uuid\\:1}&securedaction="+action;
	$.get(url, bind(function(data) {
		if (this.actionDialog) {
			this.actionDialog.dialog('open');
		} else {
			var items = mapJQuerySelector(function (item) {
				return items;
			},$("#rightsForAction"));

			if (items || items.length == 0) {
				$(document.body).append('<div id="rightsForAction"></div>');
			}
		    
			
		    this.actionDialog = $('#rightsForAction').dialog({
		        width:640,
		        height:480,
		        modal:true,
		        title:"#title",
		        buttons: {
		            "Close": bind(function() {
		        		this.actionDialog.dialog("close");
		            },this)
		        }
		    });
		    
		}
		$("#rightsForAction").html(data);
	}, this));		    	
}

/** Open global actions dialog */
GlobalActions.prototype.globalActions=function() {
	// change affected pids
	affectedObjectsRights.pids = [{pid:'uuid:1',model:'REPOSITORY'}];
		
	var url = "inc/admin/_global_actions.jsp";
	$.get(url, bind(function(data) {
		if (this.dialog) {
			this.dialog.dialog('open');
		} else {
			var items = mapJQuerySelector(function (item) {
				return items;
			},$("#globalActions"));

			if (items || items.length == 0) {
				$(document.body).append('<div id="globalActions"></div>');
			}
		    
		    this.dialog = $('#globalActions').dialog({
		        width:640,
		        height:480,
		        modal:true,
		        title:"#title",
		        buttons: {
		            "Close": bind(function() {
		        		this.dialog.dialog("close");
		            },this)
		        }
		    });
		    
		}
		
		$("#globalActions").html(data);
	}, this));
}


var globalActions = new GlobalActions();


/** 
 * Operation which we'll do
 * @param name
 * @returns {Operation}
 */
function Operation(name) {
	this.name = name;
}

/** create right */
var CREATEOP = new Operation("create");
/** edit right */
var EDITOP = new Operation("edit");
/** delete right */
var DELETEOP = new Operation("delete");
