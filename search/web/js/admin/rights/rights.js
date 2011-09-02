/**
 * @fileoverview <h3>Rights administration module</h3>
 */

/** object that can manage rights */
var affectedObjectsRights = null;

function AffectedObjectsRights() {
	this.dialog = null;
	this.pids=[];
	this.securedActionTabs = {};
	
	this.openedDetails=[];
	
}

AffectedObjectsRights.prototype.onChange=function(pid) {
	
	var npids = [];
	$("#rightsAffectedObject_selected input:checked").each(function(index,val) {
		var v = $(val).val();
		npids.push( {pid:v,model:''} );
	});
	this.pids = npids;

	for(var tab in this.securedActionTabs) {
		this.securedActionTabs[tab].dirty=true;	
		this.securedActionTabs[tab].retrieveUrl = this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+tab;
	}
}

/** opens dialog for displaying selected objects 
 */
AffectedObjectsRights.prototype.openDialog = function(/** array of struct */pids) {
	
	this.pids = pids;
	var url = this.url("inc/admin/_display_objects_dialog.jsp?pids=");
	
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
        		var url = this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+action;
        		this.securedActionTabs[action] = new SecuredActionTab( {
        			securedAction:action,
        			retrieveUrl: url,
        			pids:this.pids
        		});
        		this.securedActionTabs[action].url = bind(this.url, this);
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


	/** 
	 * Operation which we'll do
	 * @param name
	 * @returns {Operation}
	 */
	function Operation(name) {
		this.name = name;
	}
	/** create right */
	this.createop = new Operation("create");
	/** edit right */
	this.editop = new Operation("edit");
	/** delete right */
	this.deleteop = new Operation("delete");
	
	/** op*/
	this.operation = this.createop;
} 


SecuredActionTab.prototype.globalEdit = function() {
	this.operation = this.editop;
	var editUrl = this.url("inc/admin/_display_rights_for_edit.jsp?pids=")+"&securedaction="+this.securedAction;
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
	this.operation = this.deleteop;

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
SecuredActionTab.prototype.retrieve = function() {
	$("#"+this.securedAction+"_waiting").html("Nacitani...");
	
	$.get(this.retrieveUrl, bind(function(data){
    	$('#rightsAffectedObject_'+this.securedAction).html(data);
    	$("#"+this.securedAction+"_waiting").html("");
	},this));

	this.dirty = false;
}

SecuredActionTab.prototype.newRight = function() {
	this.operation = this.createop;
	var url = this.url("inc/admin/_new_right.jsp?pids=")+"&securedaction="+this.securedAction;
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
	this.operation = this.createop;
	var arr = toStringArray(path);
	var url = this.url("inc/admin/_new_right.jsp?pids=",[{pid:arr[arr.length-1].trim()}])+"&securedaction="+this.securedAction;
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

SecuredActionTab.prototype.editRightForPath=function(/** ident for right */rightId, path) {
	this.operation = this.editop;
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
	this.operation = this.deleteop;
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
