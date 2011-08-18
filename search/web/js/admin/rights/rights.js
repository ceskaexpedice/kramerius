/**
 * @fileoverview <h3>Rights administration module</h3>
 */

/** object that can manage rights */
var affectedObjectsRights = null;

function AffectedObjectsRights() {
	this.dialog = null;
	this.pids=[];
	this.securedActionTabs = {};
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
                title:"Bez titulku..",
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
        			retrieveUrl: url
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
AffectedObjectsRights.prototype.url = function(/** String */baseUrl) {
	return baseUrl+"{"+reduce(function(base, item) {
    	base = base+item.pid.replaceAll(":","\\:")+";";
        return base;
    }, "",this.pids)+"}";        
}


/** change display 
 */
AffectedObjectsRights.prototype.displayDetails = function(id) {
	
	if ($("#"+id).is(':visible')) {
		$("#"+id+"_icon").removeClass('ui-icon ui-icon-triangle-1-s folder');
		$("#"+id+"_icon").addClass('ui-icon ui-icon-triangle-1-e folder');
	} else {
		$("#"+id+"_icon").removeClass('ui-icon ui-icon-triangle-1-e folder');
		$("#"+id+"_icon").addClass('ui-icon ui-icon-triangle-1-s folder');
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

	this.securedAction = struct.securedAction;
	this.retrieveUrl = struct.retrieveUrl;
	
	this.newRightDialog = null;
} 


/** refreshing content
 */
SecuredActionTab.prototype.retrieve = function() {
	$.get(this.retrieveUrl, bind(function(data){
    	$('#rightsAffectedObject_'+this.securedAction).html(data);
	},this));

	this.dirty = false;

	
}

SecuredActionTab.prototype.newRight = function() {
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
                		$.post("rights?action=create", flatten({data:rightContainer.data,affectedObjects:rightContainer.affectedObjects}));
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



SecuredActionTab.prototype.newRightForPath = function(path) {
	// new right for path
	var arr = toStringArray(path);
}

SecuredActionTab.prototype.editRightForPath=function(/** ident for right */rightId, path) {
	
}

SecuredActionTab.prototype.deleteRightForPath=function(/** ident for right */rightId, path) {
	alert("delete right '"+rightId+" and path "+path);
}



var rightContainer = {};

var objects = {};



function toStringArray(str) {
	var nstr = str.substring(1,str.length-2);
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
			} else {
				retval[cprefix+item]=cstruct[item]; 				
			}
		}

	}
	return retval;
}
