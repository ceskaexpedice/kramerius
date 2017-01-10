<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<view:object name="adminMenuViewObject" clz="cz.incad.Kramerius.views.adminmenu.AdminMenuViewObject"></view:object>

<style type="text/css">
    #adminMenu{
        padding:5px;
        display: none;
        position:absolute;
        right:0;
        z-index:100;
        width:380px;
        border: 1px solid gray;
    }
    
    #adminMenuItems{
        padding:5px;
    }
    #adminMenuItems>div{
        padding:2px;
    }
    
    #adminMenuItems > span {
    display: block;
    float: left;
    height: 16px;
    overflow: hidden;
    text-indent: -99999px;
    width: 16px;
}

    #adminMenu>div.header{
        height:20px;
        text-align: center;
        width:100%;
        font-weight: bold;
    }
    #adminMenu>div.footer{
        text-align: right;
        width:100%;
        height:20px;
    }
     
         
</style>
<div id="adminMenu" class="shadow ui-widget-content">
    <div class="header"><view:msg>administrator.menu</view:msg></div>
    
    <div id="adminMenuItems" class="adminMenuItems">

        <c:forEach var="part" items="${adminMenuViewObject.mainMenu.parts}" varStatus="status">
            <c:if test="${part.renderable}">
                <c:forEach var="item" items="${part.items}" >
                    <c:if test="${item.renderable}">
                    <span class="ui-icon ui-icon-triangle-1-e">item</span>
                        ${item.renderedItem}
                    </c:if>
                </c:forEach>
                <c:if test="${not status.last}"> <hr/> </c:if>
            </c:if>
        </c:forEach>

    </div>
    <div class="footer">
        <input type="button" value="<view:msg>common.close</view:msg>" class="ui-state-default ui-corner-all"  onclick="hideAdminMenu();" />
    </div>
</div>


    
<!-- vypis procesu -->
<div id="processes" style="display:none;"></div>

<!-- confirmation dialog -->
<div id="confirm_dialog" title="Potvrdit" style="display:none;">
	<img src="img/alert.png" alt="alert" />

        <span id="proccess_confirm_text"></span>
</div>

<!-- administrace virtualnich sbirek -->
<div id="vcAdminDialog" style="display:none;">
    <div class="content"><fmt:message bundle="${lctx}" key="administrator.dialogs.waiting" /></div>
</div>

<!-- indexace dokumentu -->
<div id="indexer" style="display:none;">
    <div id="indexerContent"><fmt:message bundle="${lctx}" key="administrator.dialogs.waiting" /></div>
</div>

<!-- common -->
<div id="common_started" style="display:none;">
	<div id="common_started_waiting" style="margin: 16px; font-family: sans-serif; font-size: 10px; ">
    	<table>
    		<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>
			<tr><td align="center" id="common_started_text"></td></tr>
    	</table>
	</div>
	<div id="common_started_ok" style="margin: 12px;display:none;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_ok"><br/></p>
	</div>
	<div id="common_started_failed" style="margin: 12px;display:none;">
		<p style="font-family: sans-serif; font-size: 12px; font-weight: bold;" id="common_started_text_failed"></p>
	</div>
</div>

<script type="text/javascript">
    /* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function(){
    $('body').click(function() {
    	hideAdminMenu();
        //hideContextMenu();
    });
});

function showConfirmDialog(t,f){
    $("#confirm_dialog").dialog('destroy');
    $( "#proccess_confirm_text" ).html(t);
    $( "#confirm_dialog" ).dialog({
        resizable: false,
        height:140,
        modal: true,
        buttons: [{
            text:dictionary['common.ok'],
            click:function() {
                $(this).dialog('destroy');
                f();
            }
        },{
            text:dictionary['common.close'],
            click:function() {
                $(this).dialog('destroy');
            }
        }]
    });
}

function showAdminMenu() {
	$("#adminMenu").css("top",$("#header").offset().top + $("#header").height()+4);
	$("#adminMenu").show();
}

function hideAdminMenu() {
	$("#adminMenu").css("display","none");
}




function enumerator() {
    showConfirmDialog(dictionary['administrator.dialogs.enumerator.confirm'], function(){
        noParamsProcess('enumerator');
    });
}

function indexmigrations() {
    showConfirmDialog(dictionary['administrator.dialogs.migrationindex.confirm'], function(){
        noParamsProcess('migrationindex');
    });
}


function replicationRights() {
    showConfirmDialog(dictionary['administrator.dialogs.replicationrights.confirm'], function(){
        noParamsProcess('replicationrights');
    });
}


function convert() {
    showConfirmDialog(dictionary['administrator.dialogs.convert.confirm'], function(){
        noParamsProcess('convert');
    });
 
}

function movingwallOverRepo() {
    /*
    showConfirmDialog(dictionary['administrator.dialogs.mw.confirm'], function(){
        noParamsProcess('iterated_appliedmw');
    });
    */
}

function impor() {
    showConfirmDialog(dictionary['administrator.dialogs.import.confirm'], function(){
        noParamsProcess('import');
    });
}

function importMonographs() {
    showConfirmDialog(dictionary['administrator.dialogs.importMonograph.confirm'], function(){
		var url = "lr?action=start&def=replikator_monographs&out=text";
	    processStarter("import").start(url);
    });
}


function importPeriodicals() {
    showConfirmDialog(dictionary['administrator.dialogs.importPeriodical.confirm'], function(){
        var url = "lr?action=start&def=replikator_periodicals&out=text";
        processStarter("import").start(url);
    });
}

function replaceAll(txt, replace, with_this) {
	  return txt.replace(new RegExp(replace, 'g'),with_this);
}

/**
 * Reindexace
 * @param level
 * @param model
 * @return
 */
function reindex(level, model) {
	hideAdminOptions(level);
	var uuid = $("#tabs_"+level).attr('pid');
	var title = $("#tabs_"+level + ">div>div[id=info-"+model+"]").text();
	var escapedTitle = replaceAll(title, ',', '');
	escapedTitle = replaceAll(escapedTitle, '\n', '');
        escapedTitle = escapedTitle.replace(/ +(?= )/g,'');

	var url = "lr?action=start&def=reindex&out=text&params=reindexDoc,"+uuid+","+escapedTitle;

	processStarter("reindex").start(url);
    
}



function exportTOFOXML(level)  {
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	var pidpath = COMMON.pidpath(level);
	var url = "lr?action=start&def=export&out=text&params="+pid;

	processStarter("foexport").start(url);
}


function noParamsProcess(process)  {
	var url = "lr?action=start&def="+process+"&out=text";
    processStarter(process).start(url);
}

function deleteUuid(level, model)  {
	hideAdminOptions(level);
 
	showConfirmDialog(dictionary['administrator.dialogs.deleteconfirm'], function(){
		var pid = $("#tabs_"+level).attr('pid');
		var pidpath = COMMON.pidpath(level);
		var url = "lr?action=start&def=delete&out=text&params="+pid+","+pidpath;
	
	   processStarter("delete").start(url);
			
	});
}

/**
 * Generovani staticke exportu
 * @param level
 * @return
 */
function generateStatic(level, exportType, imgUrl, i18nUrl,iso3Country, iso3Lang){
	hideAdminOptions(level);
	var pid = $("#tabs_"+level).attr('pid');
	var url = "lr?action=start&def="+exportType+"&out=text&params="+pid+","+imgUrl+","+i18nUrl+","+iso3Country+","+iso3Lang;

    processStarter("staticPDF").start(url);
	 
}


var _vcAdminDialog;
function showVirtualCollectionsAdmin(){
    $("#vcAdminDialog>div.content").html('<p align="center"><img src="img/loading.gif" alt="loading" /></p>');
    if (_vcAdminDialog) {
        _vcAdminDialog.dialog('open');
    } else {
    	_vcAdminDialog = $("#vcAdminDialog").dialog({
            bgiframe: true,
            width: 700,
            height: 400,
            modal: true,
	        title: dictionary['administrator.menu.dialogs.virtualcollections.title'],
            buttons: [{
                text:dictionary['common.close'],
                click: function() {
                    $(this).dialog("close") 
                } 
            }]
        });
    }
    $("#vcAdminDialog>div.content").load("inc/admin/_virtual_collection_admin.jsp");
}



var _indexerDialog;
/**
 * Zobrazuje spravu indexace
 */
function showIndexerAdmin(){
    hideAdminMenu();
    var url = "inc/admin/_indexer_data.jsp?offset=0";
    $.get(url, function(data) {
        $("#indexerContent").html(data);
        checkIndexed();
    });
    if (_indexerDialog) {
        _indexerDialog.dialog('open');
        _indexerDialog.dialog("option", "width", $(window).width()-20);
        _indexerDialog.dialog("option", "height", $(window).height()-60);
    } else {
    	_indexerDialog = $("#indexer").dialog({
            bgiframe: true,
            width: $(window).width()-20,
            height: $(window).height()-60,
            modal: true,
	        title: dictionary['administrator.menu.dialogs.indexDocuments.title'],
            buttons: [{
                text:dictionary['common.close'],
                click: function() {
                    $(this).dialog("close") 
                } 
            }]
        });
    }
}

function checkIndexed(){
    var url;
    var pid;
    $('.indexer_result').each(function(){
        pid = $(this).attr('pid');
        var prefix = "info\:fedora\/";
        pid = pid.replace(prefix,"");
        var obj = this;
        url = "inc/admin/_indexer_check.jsp?pid="+pid;
        $.get(url, function(data) {
            if(trim10(data)=="1"){
                var el = $(obj).children('td:eq(0)');
              $(el).addClass("indexer_result_indexed");
              $(el).attr("title", "view document");
            }else{
               $(obj).children('td:eq(0)').addClass("indexer_result_notindexed"); 
            }
        });
    });
}

function deletefromindex(level){
	hideAdminOptions(level);
    showConfirmDialog('Confirm delete dokument from index', function(){
       var pid = $("#tabs_"+level).attr('pid');
       var pid_path = "";
       for(var i = level; i>0; i--){
           pid_path = $('#tabs_'+i).attr('pid') + pid_path;
           if(i>1) {pid_path = '/' + pid_path};
       }
       var url = "lr?action=start&def=reindex&out=text&params=deleteDocument,"+pid_path+","+pid;
       processStarter("delindex").start(url);
    });
}

function confirmIndexDocByPid(pid){
    var url = "inc/admin/_indexer_get_title.jsp?pid="+pid;
    $.get(url, function(data) {
        showConfirmDialog('Confirm index dokumentu: ' + data, function(){
          //var prefix = "info\:fedora\/uuid:";
          //var uuid = pid.replace(prefix,"");
          var escapedTitle = replaceAll(data, ',', '');
          var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+pid+","+escapedTitle;
          processStarter("reindex").start(url);
        });        
    }).error(function(){
        alert("PID not found");
    });
}

function confirmIndexModel(model){
    showConfirmDialog('Confirm index model: ' + model, function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      processStarter("reindex").start(url);
    });
}

function checkIndexIntegrity(){
var text = dictionary['administrator.dialogs.confirm'] + " " + dictionary['administrator.menu.dialogs.check_integrity'];
    showConfirmDialog(text, function(){
      var url = "lr?action=start&def=reindex&out=text&params=checkIntegrity,check,Check integrity";
      processStarter("reindex").start(url);
    });
}

function indexDoc(pid, title){
    showConfirmDialog('Confirm index dokumentu', function(){
    var prefix = "info\:fedora\/";
    var pid2 = pid.replace(prefix,"");
      var escapedTitle = replaceAll(title, ',', '');
      var url = "lr?action=start&def=reindex&out=text&params=fromKrameriusModel,"+pid2+","+escapedTitle;
      processStarter("reindex").start(url);
    });
}

function indexModel(model){
    showConfirmDialog('Confirm index cely model', function(){
      var url = "lr?action=start&def=reindex&out=text&params=krameriusModel,"+model+","+model;
      processStarter("reindex").start(url);
    });
}

function getAllowed(action, pids, div){
    var s = "<ul>";
    var url = "isActionAllowed?action="+action;
    for(var i=0; i<pids.length; i++){
        url += "&pid=" + pids[i];
    }
    $.getJSON(url, function(data) {
        $.each(data, function(key, val) {
            s += '<li id="' + key + '">' + val + '</li>';
        });
        s += '</ul>';
        $(div).html(s);
    });
}


function ShowSearchHistory() {
    this.dialog = null;     
}


ShowSearchHistory.prototype.showHistory = function() {
    $.get("profile?action=GET", bind(function(data){
        
        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            var pdiv = '<div id="searchHistory"></div>';
            $(document.body).append(pdiv);
            this.dialog = $("#searchHistory").dialog({
                bgiframe: true,
                width:  800,
                height:  400,
                modal: true,
                title: '',
                buttons: 
                    [{
                        text:dictionary['common.close'],
                        click:function() {
                            $(this).dialog("close") 
                        } 
                    }]
            });

        }

        var htmlheader = "<table style='width:100%'>"+
        "<thead><tr>"+
        "<td><h3>"+dictionary['administrator.menu.dialogs.profile.searchedWords']+"</h3></td>"
        +"<td><h3>"+dictionary['administrator.menu.dialogs.profile.searchedUrl']+"</h3></td>"
        +"<td><h3>"+dictionary['administrator.menu.dialogs.profile.searchedRSS']+"</h3></td>"
        +"</tr> </thead>"+
        "<tbody>";


        function keys(elm) {
            var ret = [];
            for(var key in elm) {
                ret.push(key);
            }
            return ret;
        }

        function field(element, arr, field, key) {
            var f = element[field];
            if (f) {
                return "<strong>"+(key ?  dictionary[key] : dictionary[field]) +"</strong>:"+f; 
            } else return "";
         }
        
        function facet(element,arr) {
        	var fq_index = arr.indexOf("fq");
            if (fq_index >= 0) {
            	var fq = element['fq'];
                var retval = reduce(function(base, fqel, status) {
                    var splitted = fqel.split(':');
                    var type = splitted[0];
                    var val = splitted[1];
                    if (val.startsWith('"') && val.endsWith('"')) {
                        val = val.substring(1,val.length-1);
                    }
                    return base+"<strong>"+dictionary["facet."+type]+"</strong>:"+ (dictionary[val] ? dictionary[val]:val) +(status.last ? "" : " ");                
                },"",fq);
                return retval;
                
            } else return "";
        }

        function casovaosa(element,arr) {
            var da_od_index = arr.indexOf("da_od");
            var da_do_index = arr.indexOf("da_do");
            if (da_od_index >= 0 && da_do_index>=0) {
                var da_od = element["da_od"];
                var da_do = element["da_do"];
                if (da_od_index >= 0) arr.rm(arr.indexOf("da_od")); 
                if (da_do_index >= 0) arr.rm(arr.indexOf("da_do")); 
                return "<strong>"+dictionary['common.date']+"</strong>:"+da_od+"-"+da_do; 
            } else return "";
        }

        function query(element,arr) {
            var q = element["q"];
            if (q) {
                return "<strong>"+dictionary['filter.query']+"</strong>"+q;
            } else return "";
        }

        function append(prev, nval) {
            if (nval.length > 0 && prev.length > 0) {
                prev = prev +", ";
            }
            prev = prev+nval;
            return prev;
        }
        
        var html = data['searchHistory'] ? reduce(function(base, element, status) {
           var k = keys(element);
           var el = casovaosa(element,k);
           el = append(el, query(element,k));
           el = append(el, facet(element,k));
           
           el = append(el,field(element,k,'title'));
           el = append(el,field(element,k,'rok'));
           el = append(el,field(element,k,'issn'))
           el = append(el,field(element,k,'author'));
           el = append(el,field(element,k,'udc'));
           el = append(el,field(element,k,'ddc'));
           el = append(el,field(element,k,'browse_title','suggest.search_title'));
           
        	base = base + "<tr>"+
            "<td>"+ el +"</td>"+
            "<td> <a class='ui-icon ui-icon-link' href='"+ element["url"]+"&fromProfile=true'>_link</a></td>"+
            "<td> <a class='ui-icon ui-icon-signal-diag' href='"+ element["rss"]+"&fromProfile=true'></a></td>"+
            "</tr>";       
            return base;         
        }, htmlheader, data['searchHistory'].reverse())+"</tbody></table>" : htmlheader;
                   
        $("#searchHistory").html(html);
        
    },this));
}

var showSearchHistory = new ShowSearchHistory();

/**
 * Saving profile dialog
 */
function SaveProfile() {
    this.dialog = null;
    this.savingFields = []
    //"results":{"columns":1,"sorting":"title","sorting_dir":"asc"}
    this.ops = {
         "sorting_dir": bind(function(profile, val){
             if (!profile["results"]) {
                 profile["results"] = {};
             }
             var results = profile["results"];
             results['sorting_dir'] = val;
         },this),
         "columns":bind(function(profile, val){
             if (!profile["results"]) {
                 profile["results"] = {};
             }
             var results = profile["results"];
             results['columns'] = val;
         },this),
         "sorting":bind(function(profile, val){
             if (!profile["results"]) {
                 profile["results"] = {};
             }
             var results = profile["results"];
             results['sorting'] = val;
         },this),
         "client_locale":bind(function(profile,val){
        	 profile['client_locale'] = val;
         },this)
    };
    //sorting_dir,columns,sorting,client_locale
}



SaveProfile.prototype.saveProfile = function() {
    $.get("inc/_save_profile.jsp", bind(function(data){

        	this.savingFileds = [];
         
            if (this.dialog) {
                this.dialog.dialog('open');
            } else {
                var pdiv = '<div id="saveProfile"></div>';
                $(document.body).append(pdiv);
                this.dialog = $("#saveProfile").dialog({
                    bgiframe: true,
                    width:  600,
                    height:  350,
                    modal: true,
                    title: dictionary['userprofile.forsave.saveprofiletitle'],
                    buttons: 
                        [{
                             text:dictionary['common.save'],
                             click:bind(function() {
                            	 this.dialog.dialog("close");
                                 $("#saveProfile input:checked").each(bind(function(index,item) {
                                     var id = $(item).attr("id");
                                     this.savingFields.push(id);
                                 },this));


                                 // prepare fields to session                                 
                                var url = reduce(function(base, item, status) {
                                    var val = $("#"+item).val();
                                    return base+"&key="+item+"&field="+val; 
                                }, "profile?action=PREPARE_FIELD_TO_SESSION", this.savingFields);
                                $.get(url, function() {});
                                 

                                 // saving profile
                                 (new Profile()).modify(bind(function(data) {
                                     this.savingFields.forEach(bind(function(item) {
                                         var val = $("#"+item).val();
                                         if (this.ops[item]) {
                                        	 this.ops[item](data,val);
                                         }
                                     },this));
                                     return data;
                                  },this), bind(function() {

                                  }, this));
                                 
                             },this) 
                         },
                         {
                            text:dictionary['common.close'],
                            click:function() {
                                $(this).dialog("close");
                            } 
                        }]
                });

            }
            $("#saveProfile").html(data);
        	
     },this));
}

var saveProfile = new SaveProfile();




/** 
 * Parametrized processes
 */
function ParameterizedProcess() {
    // input form dialog
    this.dialog = null;
    // wait dialog
    this.waitDialog = null;
}

ParameterizedProcess.prototype._asArr=function(struct) {
    var arr = [];
    for (var key in struct) { arr.push(key+"="+struct[key]); } 
    return arr;        	 
}


ParameterizedProcess.prototype.openWaitDialog = function() {
    if (this.waitDialog) {
        this.waitDialog.dialog('open');
    } else {
        var pdiv ='<div id="parametrized_process_form_wait">'+
                    "<div style=\"margin: 16px; font-family: sans-serif; font-size: 10px;\">"+
                        "<table style='width:100%'>"+
                            "<tbody>"+
                            "<tr><td align=\"center\"><img src=\"img/loading.gif\" height=\"16px\" width=\"16px\"></td></tr>"+
                            "<tr><td align=\"center\">"+dictionary['parametrizedprocess.dialog.waitForm']+"</td></tr>"+
                            "</tbody>" +
                        "</table>" +
                    "</div>"+
                  '</div>';
            
        $(document.body).append(pdiv);
        this.waitDialog = $("#parametrized_process_form_wait").dialog({
            bigframe: true,
            width:  500,
            height:  300,
            modal: true,
            title: dictionary['common.waitplease'],
            buttons: [{
                          text:dictionary['common.close'],
                          click:bind(function() {
                             this.waitDialog.dialog("close"); 
                          },this)
            }]
        });
    }
}

/**
 * opens parameters dialog
 */
ParameterizedProcess.prototype.open = function(definition, paramsMapping) {

    this.openWaitDialog();
    
    paramsMapping = paramsMapping ? paramsMapping : {};

    var pMappingsUrl = "{"+reduce(function(base, element, status) {
    	if (!status.first) {
        	base = base + ";";
        }
    	base = base + element;
    	return base; 
    }, "", this._asArr(paramsMapping))+"}";
    		  
    var url = "lr?action=form_get&def="+definition;

    if (pMappingsUrl) url = url+"&paramsMapping="+pMappingsUrl;

    $.get(url , bind(function(data){
        
        this.waitDialog.dialog('close');
        
    	if (this.dialog) {
    		this.dialog.dialog('open');
        } else {
            var pdiv = '<div id="parametrized_process"></div>';
            $(document.body).append(pdiv);
            this.dialog = $("#parametrized_process").dialog({
                bgiframe: true,
                width:  700,
                height:  500,
                modal: true,
                title: '',
                buttons: [
                    {
                        text: dictionary['common.start'],
                        click: bind(function() {
                            window.onProcessFormSend();
                            this.dialog.dialog("close"); 
                        }, this)
                    },
                    {
                        text: dictionary["common.close"],
                        click:bind(function() {
                            this.dialog.dialog("close"); 
                        },this) 
                    }
                ]
                    
            });
        }
    	$("#parametrized_process").dialog( "option", "title", dictionary['parametrizedprocess.dialog.title'] );
        $("#parametrized_process").html(data);
    }, this));
}


var parametrizedProcess = new ParameterizedProcess();


</script>