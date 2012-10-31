/**
 * @fileoverview <h3>One right edit</h3>
 */

/** One right */

function Right() {
	this.paramTabs = {
		"create": new ParamsTab("create","createParams", bind(function() {
			rightContainer.data.param.ident=-1;
			rightContainer.data.param.shortDesc='';
			rightContainer.data.param.objects=[];
			this.rebuildParamsTab();
		},this)),
		"edit":  new ParamsTab("edit","editParams", bind(function() {
			$("#usedParams").val(rightContainer.data.param.ident);
			this.rebuildParamsTab();
		},this))
	};
}
/** change radio button event */
Right.prototype.onUserRadioChange = function() {

    var rb = $("input:radio:checked");
	if (rb.val() === "role") {
		$("#userIdDiv").show();
		var v = $("#rolecombo").val();
		this.roleSelection(v);
	} else {
	    //TODO: do it better
    	this.roleSelection("common_users");
		$("#userIdDiv").hide();
	}
}

/** change criterium combo event */
Right.prototype.onCriteriumChange = function() {
	var selected = $("select[name='criterium'] option:selected").val();
    if (selected) {
        this.rebuildParamsTab();
        $("#rightParamsCreation").show();
    } else {
        $("#rightParamsCreation").hide();
    }   
    
    rightContainer.data.condition=selected;
    // kriterium ma/nema povoleny parametry
    if (rightContainer.options.criteriums[selected] && rightContainer.options.criteriums[selected].paramsNecessary) {
        this.rebuildParamsTab();
        $("#rightParamsCreation").show();
    } else {
        $("#rightParamsCreation").hide();
    }
    
}

/** change role event  (keyup) */
Right.prototype.onRolesKeyUp=function(elm) {
	var userVal = $(elm).val();
    rightContainer.data.role=userVal;
//	var narr = map(function(item) {
//		if (item.startsWith(userVal)) {
//			return item;                                           	    		    
//		} else return null;                                        	    	   
//	},rightContainer.options.roles);

//	if (narr.length > 0) {
//	    var nhtml = "";
//	    	narr.forEach(function(item) {
//	    		var ritem = item.replaceAll('"','\\"');
//	    		nhtml = nhtml + "<div><a href='javascript:right.roleSelection(\""+ritem+"\");'>"+item+"</a></div>";
//		});
//   	$("#userautocomplete").html(nhtml);                                              
//    	$("#userautocomplete").show();
//	}
}

Right.prototype.roleSelection = function(what) {
//    $("#userId").val(what);                                        	    
//    $("#userautocomplete").hide();
    rightContainer.data.role=what;
}

/** on priority change event */
Right.prototype.onPriorityChange = function() {
	rightContainer.data.priority = $("#priority").val();
}


Right.prototype.initUI = function(/* right container */rightContainer) {

    $("#tabs").tabs({
		select: bind(this.onTabChange,this)
	});

	if (!rightContainer.data.justcreated) {
		if (rightContainer.data.role !== 'common_users') {

			$("#roleType").attr("checked", "checked");

			$("#roleType").each(bind(function(elm) {
				this.onUserRadioChange(elm);
			},this));
			$("#userId").val(rightContainer.data.role);
		}

		if (rightContainer.data.condition) {
			$("#criterium").val(rightContainer.data.condition);
		}

	    if (rightContainer.options.criteriums[rightContainer.data.condition] && rightContainer.options.criteriums[rightContainer.data.condition].paramsNecessary) {
			$("#rightParamsCreation").show();
			$("#params").val(rightContainer.data.param.ident);
	    }
	    
		
		if (rightContainer.data.param.ident != -1) {
	    	$("#tabs").tabs( "select" , 1);
		} else {
	    	$("#tabs").tabs( "select" , 0);
		}
		
		this.rebuildParamsTab();
	} else {
    	$("#tabs").tabs( "select" , 0);
	}
}


Right.prototype.onTabChange = function(event, ui) {
	var key = $(ui.tab).data("key");
	this.paramTabs[key].tabSelection();
	
	$('input[name=tab]').attr('checked',false);
	$("#"+key+"-check").attr('checked', true);
}

Right.prototype.rebuildParamsTab = function() {
	for(var key in this.paramTabs) {
		this.paramTabs[key].rebuild();
	}
}


function ParamsTab(key, paramsDiv, tabSelection) {
	this.paramsDiv = paramsDiv;
	this.key = key;
	
	this.tabSelection = tabSelection || bind(function() {},this);
}

ParamsTab.prototype.rebuildTable = function() {
	$("#" + this.paramsDiv + " tr").remove();
	$(rightContainer.data.param.objects)
			.each(
		bind(
				function(i, val) {
					var tr ="<tr id='"+i+ "'> <td width='100%'>"+val+"</td> <td><a href='javascript:right.paramTabs[\""+this.key+"\"].remove("+ i+ ");' class='ui-icon ui-icon-minus'></a></td> <td> <a href='javascript:right.paramTabs[\""+this.key+"\"].edit("+ i+ ");' class='ui-icon ui-icon-pencil'></a> </td></tr>";
					$("#" + this.paramsDiv).append(tr);
				}, this));
}

ParamsTab.prototype.remove = function(index) {
	rightContainer.data.param.objects.rm(index);
	this.rebuildTable();
}

ParamsTab.prototype.edit = function (index) {
	var val = rightContainer.data.param.objects[index];
	new InputTextDialog({
		label : 'Editovana hodnota:',
		value : val
	}).open(bind(function(cVal) {
		rightContainer.data.param.objects[index] = cVal;
		this.rebuildTable();
	}, this));
	
}

ParamsTab.prototype.add = function() {
	new InputTextDialog({
		label : 'Nova hodnota:',
		value : ''
	}).open(bind(function(cVal) {
		rightContainer.data.param.objects.push(cVal);
		this.rebuildTable();
	}, this));
}

ParamsTab.prototype.rebuildLabel = function() {
	var value = rightContainer.data.param.shortDesc || 'Nepojmenovano';
	$("#" + this.paramsDiv + "Label").text(value);
}


ParamsTab.prototype.editLabel = function() {
	new InputTextDialog({
		label : 'Nazev:',
		value : rightContainer.data.param.shortDesc
	}).open(bind(function(cVal) {
		rightContainer.data.param.shortDesc= cVal;
		this.rebuildLabel();
	}, this));
	
}



ParamsTab.prototype.rebuild = function() {
	this.rebuildLabel();
	this.rebuildTable();
}


ParamsTab.prototype.onSelectParams = function() {
	var ident = $("#usedParams").val();
	var reduced = reduce(function(base, element, status){
		if (base == null) {
			if (element.ident == ident) {
				return element;
			}
		} else return base;
	}, null, rightContainer.options.params);
	
	if (reduced) {
		rightContainer.data.param = reduced;
	} else {
		rightContainer.data.param.ident = -1;
		rightContainer.data.param.shortDesc = '';
		rightContainer.data.param.objects = [];
	}
	this.rebuild();
}