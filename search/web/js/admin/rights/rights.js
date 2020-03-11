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
            title:'',
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

ChangePswd.prototype.waiting=function() {
	$('#changePswd_form').hide();
	$("#checkPswdStatus").hide();
	$('#changePswd_wait').show();
}

ChangePswd.prototype.form=function() {
	$('#changePswd_form').show();
	$("#checkPswdStatus").show();
	$('#changePswd_wait').hide();
}
ChangePswd.prototype.postChangedPswd=function() {
     var xhr = $.post("users?action=savenewpswd", { nswpd:$("#pswd").val(),opswd:$("#oldpswd").val()},
        bind(function (data,textStatus) {
                if (textStatus =="success") {
                        this.dialog.dialog("close"); 
                        $("#changePswd").remove();
                } else {
                    $('#changePswd_form').show();
                    $('#changePswd_wait').hide();
                    $("#checkPswdStatus").css('color','red');
                    $("#checkPswdStatus").show();
                    $("#checkPswdStatus").text(dictionary['rights.changepswd.nochangepswd']);
                }
        },this));
     
     xhr.fail(function (jqXHR, textStatus, errorThrown) {
         $('#changePswd_form').show();
         $('#changePswd_wait').hide();
         $("#checkPswdStatus").css('color','red');
         $("#checkPswdStatus").show();
         $("#checkPswdStatus").text(dictionary['rights.changepswd.nochangepswd']);
     });
}


/** Change pswd dialog */
ChangePswd.prototype.changePassword  = function () {
	var urlForPost = "users?action=savenewpswd";
	var url = "inc/admin/_change_pswd.jsp";
    $.get(url, bind(function(data) {
    	if (this.dialog) {
    		this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="changePswd">'+'</div>');
            this.dialog = $('#changePswd').dialog({
                width:400,
                height:250,
                modal:true,
                title:dictionary['administrator.menu.dialogs.changePswd.title'],
                buttons: [
                    {
                        text: dictionary["rights.changepswd.button"],
                        click: bind(function() {
                        	if ($("#pswd").val() == $("#pswdRepeat").val()) {
                        		this.waiting();
                    			setTimeout(bind(this.postChangedPswd,this), 3000);
                        	} else {
                            	$("#checkPswdStatus").css('color','red');
                                $("#checkPswdStatus").text(dictionary['rights.changepswd.notsamepswd']);
                            }                        	
                        },this)
                    },
                    {
                        text: dictionary['common.close'],
                        click:function() {
                            $(this).dialog("close"); 
                            $("#changePswd").remove();
                        } 
                    }
                ] 
            });
        }
    	$("#changePswd").html(data);
    	$("#changePswd").dialog('option','title',dictionary['rights.changepswd.title']);
    },this));
}


function AdditionalObjectRights() {
	this.dialog = null;
	this.pids = [];
	this.securedActionsTabs = {};
}


var selectStreams = new SecuredStreamsSelection();

/**  
 * Display dialog with secured streams
 */
function SecuredStreamsSelection() {
}

SecuredStreamsSelection.prototype.openDialog=function(pids) {
	var url = urlWithPids("inc/admin/secstream/_display_secured_streams.jsp?pids=", pids);
	$.get(url, bind(function(data){
    	if (this.dialog) {
    		this.dialog.dialog('open');
    	} else {
            $(document.body).append('<div id="securedStreams"></div>')
            this.dialog = $('#securedStreams').dialog({
                width:600,
                height:400,
                modal:true,
                title:"",
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
    	$('#securedStreams').html(data);
	}, this));
	
}


// affectedObjectRights for streams
var additionalAffectedObjectsRights = [];

/** main function for lookup dialog for rights */
function findObjectsDialog(stream) {
	if ((stream) && (stream != "default")) {
		if (!additionalAffectedObjectsRights[stream]) {
			additionalAffectedObjectsRights[stream] = new AffectedObjectsRights(['READ'],stream);
		}
		return additionalAffectedObjectsRights[stream];
	} else {
		return affectedObjectsRights;
	}
}



// default 
var affectedObjectsRights = new AffectedObjectsRights(null, "default");

/** object that can manage rights - internal or user defined streams */
function AffectedObjectsRights(actions, id) {
	this.id = id;
	this.pids=[];

	// special tabs for context menu
	this.securedActionTabs = {};
	this.openedDetails=[];

	// displayed actions
	this.actions = actions ?  actions : null;

	this.dialog = null;
}



/** On change -> event called from objects selection dialog
 */
AffectedObjectsRights.prototype.onChange=function(pid) {
	
	var npids = [];
	$("#rightsAffectedObject_selected_"+this.id+" input:checked").each(function(index,val) {
		var v = $(val).val();
		npids.push( {pid:v,model:''} );
	});
	this.pids = npids;
	
	this.clearTabs(function(tab) {
		return this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+tab;
	});
}

AffectedObjectsRights.prototype.dialogId=function() {
	return 'affectedObjectRights_'+this.id;
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
	// clear previous
	
	this.pids = pids;
	var url = this.url("inc/admin/_display_objects_dialog.jsp?pids=");

	if (this.id) { url += "&id="+this.id; }
	
	if (this.actions) {
		url=url+"&actions="+encodeURI("{")+reduce(function(base, item, status) {
	    	base = base+item+ (status.last ? "": ";");
	        return base;
	    }, "",this.actions)+encodeURI("}");
	}
	

	$.get(url, bind(function(data){
		
		if (this.dialog) {
			this.dialog.dialog('open');
    	} else {
    		$(document.body).append('<div id="'+this.dialogId()+'"></div>')
        	this.dialog = $('#'+this.dialogId()).dialog({
                width:750,
                height:450,
                modal:true,
                title:"",
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
    	
    	$('#'+this.dialogId()).html(data);
    	
    	$('a[href*="rightsAffectedObject"]').each(bind(function(index,elm) {
    		var action = $(elm).data('action');
    		if (action) {
        		this.securedActionTabs[action] = this.createSecurityActionTab(action,this.url("inc/admin/_display_rights_dialog.jsp?pids=")+"&securedaction="+action, this.id);
        		// change retrive function
        		this.securedActionTabs[action].retrieve = this.securedActionTabs[action].retrieveContextContent;
    		}
    	}, this));    

    },this));
}

/** creates secured action tab */
AffectedObjectsRights.prototype.createSecurityActionTab = function(/** String */ action, /** retrieve url */url, /** string */ id) {
	var securedActionTab = new SecuredActionTab({
		securedAction:action,
		retrieveUrl: url,
		id:id,
		pids:this.pids
	});
	securedActionTab.url = bind(this.url, this);
	return securedActionTab;
}



/** construct url from selected pids
 */
AffectedObjectsRights.prototype.url = function(/** String */baseUrl, /** Array */ pids) {
	if (!pids) pids = this.pids;
	baseUrl = baseUrl+encodeURI("{")+reduce(function(base, item, status) {
    	base = base+encodeURI(item.pid.replaceAll(":","\\:"))+ (status.last ? "": ";");
        return base;
        
    }, "",pids)+encodeURI("}");
	return baseUrl;
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
	
	this.affectedObjectsInstanceId = struct.id;
	
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
	            title:dictionary[''],
	            buttons: [
			                {
			                    text: dictionary["common.edit"], //TODO: change key
			                    click: bind(function() {
				            		$("#editRights input:checked").each(bind(function(i, val) {
				            			var arr = $(val).val().split("_");
				            			this.editRightForPath(arr[0], arr[1]);
				            		},this));
			
				            		this.globalEditDialog.dialog("close");
				            	},this)
			                },
			                {
			                    text: dictionary["common.close"], //TODO: change key
			                    click: bind(function() {
				            		this.globalEditDialog.dialog("close");
				                },this)
			                }
			                
	            	]
	            	
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
	            title:"",
	            buttons: [
	                      {
	                    	  text:dictionary['common.delete'],
	                    	  click:bind(function() {
	      	            		var rightIds= [];
	    	            		$("#delRights input:checked").each(function(i, val) {
	    	            			rightIds.push($(val).val());
	    	            		});
	    	            		rightContainer = {deletedrights:rightIds};
	            				this.post();

	    	            		this.globalDeleteDialog.dialog("close");
	    	            	},this)
	                      },
	                      	{
	                    	  text:dictionary['common.close'],
	                    	  click: bind(function() {
	    	            		this.globalDeleteDialog.dialog("close");
	    	                },this)
	                      }
                 ]
	            
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
    	$('#rightsAffectedObject_'+this.securedAction+"_"+this.affectedObjectsInstanceId).html(data);

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
                title:"",
                buttons: [
                          {
                        	  text:dictionary['common.apply'],
                        	  click:bind(function() {
                  				this.post();
                        		//$.post("rights?action=create", flatten({data:rightContainer.data,affectedObjects:rightContainer.affectedObjects}));
                        		this.newRightDialog.dialog("close");
                        	},this)
                          },
                          {
                        	 text:dictionary['common.close'],
                        	 click:bind(function() {
                         		this.newRightDialog.dialog("close");
                             },this)
                          }
                ]
                
            });
    	}
    	
    	$('#nRightDialog').html(data);
    	
	},this));
}


SecuredActionTab.prototype.post = function() {
	var struct = flatten({data:rightContainer.data,affectedObjects:rightContainer.affectedObjects, deletedrights:rightContainer.deletedrights});
	var successFunction = bind(function(){ this.retrieve(); }, this);
	$.ajax({
		  url:"rights?action="+this.operation.name, 
		  type: "POST", 
		  data: struct,
		  success:successFunction,
		  contentType: "application/x-www-form-urlencoded;charset=UTF-8",
		  dataType:"json"
	  });

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
                buttons: [{
					text:dictionary['common.apply'],
					click:bind(function() {
						this.post();
						this.newRightDialog.dialog("close");
					},this)
                  },{
					text:dictionary['common.close'],
					click:bind(function() {
						  this.newRightDialog.dialog("close");
					},this)
                  }]
            });
    	}
		
        this.newRightDialog.dialog( "option", "title", dictionary['rights.new.title'] );

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
                title:"",
                buttons: [{
                	text:dictionary['common.apply'],
                	click:bind(function() {
        				this.post();
                		this.newRightDialog.dialog("close");
                	},this)
                  },{
                	text:dictionary['common.close'],
                	click:bind(function() {
                		this.newRightDialog.dialog("close");
                	},this)
                  }]
            });
    	}
		
    	
    	$('#nRightDialog').html(data);
	},this));
	

}

SecuredActionTab.prototype.deleteRightForPath=function(/** ident for right */rightId, path) {
    showConfirmDialog(dictionary['administrator.dialogs.right.delete.confirm'], bind(function(){
    	this.operation = DELETEOP;
    	rightContainer = {deletedrights:[rightId]};
    	this.post();
    },this));

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
// volano z hlavniho menu .
GlobalActions.prototype.rigthsForAction=function(action,pid) {
	// affected rights secured actions 
	findObjectsDialog().securedActionTabs[action] = findObjectsDialog().createSecurityActionTab(action,"inc/admin/_display_rights_for_global_actions.jsp?pids="+encodeURIComponent("{uuid\\:1}")+"&securedaction="+action);
	findObjectsDialog().securedActionTabs[action].retrieve = findObjectsDialog().securedActionTabs[action].retrieveGlobalContent;
	
	var pids = (pid ? pid : "{uuid\\:1}");
	var url = "inc/admin/_display_rights_for_global_actions.jsp?pids="+encodeURIComponent(pids)+"&securedaction="+action;
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
		        width:800,
		        height:480,
		        modal:true,
		        buttons: [{
		        	text:dictionary['common.close'],
					click:bind(function() {
						this.actionDialog.dialog("close");
						},this)
		        }]
		    });
		    
		}
		this.actionDialog.dialog( "option", "title", dictionary['rights.settings.title'] );

		$("#rightsForAction").html(data);
	}, this));		    	
}

/** Open global actions dialog */
GlobalActions.prototype.globalActions=function() {
	// change affected pids
	findObjectsDialog().pids = [{pid:'uuid:1',model:'REPOSITORY'}];
		
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
		        buttons: [{
                	  text:dictionary['common.close'],
                	  click:bind(function() {
  		        		this.dialog.dialog("close");
                	  },this)
		        }]
		    });
		    
		}
    
        this.dialog.dialog( "option", "title", dictionary['rights.global.actions.title'] );

		$("#globalActions").html(data);
	}, this));
}


/** Open collection actions dialog */
GlobalActions.prototype.collectionActions=function() {
	var url = "inc/admin/_collection_actions.jsp";
	$.get(url, bind(function(data) {
		if (this.coldialog) {
			this.coldialog.dialog('open');
		} else {
			var items = mapJQuerySelector(function (item) {
				return items;
			},$("#collectionActions"));

			if (items || items.length == 0) {
				$(document.body).append('<div id="collectionActions"></div>');
			}
		    
		    this.coldialog = $('#collectionActions').dialog({
		        width:700,
		        height:400,
		        modal:true,
		        buttons: [{
                      text:dictionary['common.close'],
                      click:bind(function() {
  		                  this.coldialog.dialog("close");
                      },this)
		        }]
		    });
		    
		}
    
        //this.coldialog.dialog( "option", "title", dictionary['rights.global.actions.title'] );

		$("#collectionActions").html(data);
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




/** Criteriums search */
function CriteriumsSearch() {}

/**
 * Show all criteriums
 */
CriteriumsSearch.prototype.showCriteriums = function() {
	$.get("inc/admin/_criterium_params_manage.jsp", bind(function(data){ 
		if (this.dialog) {
			this.dialog.dialog('open');
		} else {
			$(document.body).append('<div id="criteriumsDialog"></div>')
	        this.dialog = $('#criteriumsDialog').dialog({ 
	            width:720,
	            height:480,
	            modal:true,
	            title:dictionary['rights.dialog.criteriumparams.title'],
	            buttons: [{
		              	  text:dictionary['common.close'],
		            	  click:function() {
		            		  		$(this).dialog("close"); 
            	  		  }
	            		}]
	    		});
		}
		$('#criteriumsDialog').html(data);
	},this));
}

/**
 * Show dialog with all pids
 */
CriteriumsSearch.prototype.search = function(objects) {
	if (objects.length  == 0) return;
	
	function searcher(arr, obj) {
		var found = false;
		arr.forEach(function(elm) {
			if (elm.pid === obj.pid) {
				found = true;
			}
		});
		return found;
	}
	
	var reducedObjects = reduce(function(base, object, status) {
		if (!searcher(base, object)) {
			base.push(object);
		}
		return base;
	},[], objects);
	
	
	var actions = reduce(function(base, object, status) {
		if (base.indexOf(object.action)<0) {
			base.push(object.action);
		}
		return base;
	}, [], objects);
	
	
	var d = findObjectsDialog(); 
	d.actions = actions;
	findObjectsDialog().openDialog(reducedObjects);
}

CriteriumsSearch.prototype.refresh = function() {
	$("#criteriums-manage-waiting").toggle();
	$("#criteriums-manage-content").toggle();
	$.get("inc/admin/_criterium_params_manage.jsp", bind(function(data){ 
		$('#criteriumsDialog').html(data);
	},this));
	
}

CriteriumsSearch.prototype.deleteCriterium=function(id) {
    showConfirmDialog(dictionary['administrator.dialogs.criteriumparams.delete.confirm'], bind(function(){
    	$.post("rights?action=deleteparams", {deletedparams:[id]}, this.refresh);
    },this));
}

CriteriumsSearch.prototype.renameCriterium=function(id,oldname) {
	new InputTextDialog({
		label : dictionary['common.label']+":",
		value : oldname
	}).open(bind(function(cVal) {

		$.ajax({
			  url:"rights?action=renameparams", 
			  type: "POST", 
			  data:  {renameparams:[id], name:cVal},
			  success:this.refresh,
			  contentType: "application/x-www-form-urlencoded;charset=UTF-8",
			  dataType:"json"
		  });

	}, this));

}

var criteriumsSearcher = new CriteriumsSearch();
