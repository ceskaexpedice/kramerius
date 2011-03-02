$(document).ready(function(){
    $('body').click(function() {
        hideContextMenu();
    });
    $(".menuOptions>span>a").click(function(e) {
        e.stopPropagation();
   });
    $(".adminMenuHeader a").click(function(e) {
        e.stopPropagation();
   });
});



function hideContextMenu(){
    $('.menuOptions').each(function(index){
       var id = $(this).attr('id');
       //eg id = openmenu1-monograph
       //should toggle openmenu1-monograph and menu1-monograph when not visible
       if(!$('#'+id).is(':visible')){
           $('#'+id).show();
           $('#'+id.substring(4)).hide();
       }
    });
}

var _persistentURLDialog;
function showPersistentURL(level, model) {
    hideAdminOptions(level, model);
    var pid = $("#tabs_"+level).attr('pid');
    var currentURL = window.location.href;
    if (currentURL.match("^https")=='https') {
        currentURL = currentURL.substr('https://'.length, currentURL.length); 
        var urlparts = currentURL.split('/');
        currentURL="https://"+urlparts[0]+"/"+urlparts[1]+"/";
    } else {
        currentURL = currentURL.substr('http://'.length, currentURL.length); 
        var urlparts = currentURL.split('/');
        currentURL="http://"+urlparts[0]+"/"+urlparts[1]+"/";
    }

    currentURL=currentURL+"handle/uuid:"+pid;
    var textFieldID = 'persistentURLTextField';
	
    if (_persistentURLDialog) {
        _persistentURLDialog.dialog('open');
    } else {
        $(document.body).append('<div id="persistentURL">'+
            '<span>'+dictionary['administrator.dialogs.persistenturl.text']+'</span>'+
            '<input name="'+textFieldID+'"  style="width:100%;" type="text"  maxlength="255"'+ 
            ' id="'+textFieldID+'" title="'+dictionary['administrator.menu.persistenturl']+'" /></div>');
				
        _persistentURLDialog = $('#persistentURL').dialog({
            width:640,
            height:100,
            modal:true,
            title:dictionary["administrator.menu.dialogs.persistenturl.title"],
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
	
    $('#'+textFieldID).val(currentURL);	
    $('#'+textFieldID).select();
    $('#'+textFieldID).focus(function() {
        $(this).select();
    });
}

function scrollElement(container, element){
    $(container).scrollTop($(element).offset().top - $(container).offset().top + $(container).scrollTop());
    $(container).scrollLeft($(element).offset().left);
        
}


var imgLoading = "<img src=\"img/loading.gif\" />";
var imgLoadingBig = '<div align="center" style="height:300px;padding:50%;"><img src="img/item_loading.gif" /></div>';
function trim10 (str) {
    var whitespace = ' \n\r\t\f\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000';
    for (var i = 0; i < str.length; i++) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(i);
            break;
        }
    }
    for (i = str.length - 1; i >= 0; i--) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(0, i + 1);
            break;
        }
    }
    return whitespace.indexOf(str.charAt(0)) === -1 ? str : '';
}

function _selectingPage(obj, level, model){
    var d1 = "#tabs_" + level;
    var d2 = "#tabs_" + (level-1);
    changeSelection($(d2).attr("pid"),$(obj).attr("pid"));
    showInfo($(d1+">ul>li>img"), d1, model);
}



function _changeSelectedPage(pid){
    $(".relItem[pid='" + pid + "']").each(function(i, obj){
        $(obj).parent().children(".relItem").removeClass('selected');
        $(obj).addClass('selected');
        var infoObj = $(obj).parent().parent().children("[id=info-page]");
        scrollElement($(obj).parent(), $(obj));
    });
}

function _selectItem(obj, level, model){
    if($(obj).hasClass("selected")) return;
    $(obj).parent().children(".relItem").removeClass('selected');
    
    clearThumbs();
    $(obj).addClass('selected');
    
    var d1 = "#tabs_" + level;
    var pid = $(obj).attr("pid");
    $(d1).attr('pid', pid);
    
    setSelection(level, model, pid);
   
    $(d1 + ">div>div[id=info-"+model+"]").html($(obj).text());
    var d2 = "#tabs_" + (level+1);
    var l = $(d2).tabs('length');
    for(var i=0;i<l;i++){
        $(d2).tabs("remove", 0);
    }
    var img = d1 + ">ul>li.ui-tabs-selected>img";
    showList(img, d1, model);
    
    var target = level-1;
    var p = $(d2).parent();
    $(d2).remove();
    var url ="itemMenu.jsp?pid_path="+pid+"&path="+model+"&level="+target;
    $('#imgBig').attr("src", "img/empty.gif");
    $.get(url, function(data){
        $(p).append(data);
    });
}



var COMMON = function() {
    return {
        pidpath:function(level) {
            var path ="";
            for(var i =1;i<=level;i++) {
                path = path+$("#tabs_"+i).attr('pid');
                if (i < level) {
                    path = path +"/";
                }
            }
            return path;
        }
    }
}();


var PDF=function() {

    return {
        privateLevel: 1,

        dialogSummary: null ,
        // sestaveni parametru path
        path:function(level) {
            var path ="";
            for(var i =1;i<=level;i++) {
                path = path+$("#tabs_"+i).attr('pid');
                path = path +"/";
            }
            return path;
        },
        // sestaveni url
        url:function(level) {
            hideAdminOptions(level);
            var uuid = $("#tabs_"+level).attr('pid');
            var u = "pdf?uuidFrom=" + uuid+"&uuidTo="+uuid+"&path="+PDF.path(level);
            window.location.href = u;
        },
        // sestaveni url dle parametru z dialogu
        urlFromDialog:function(level) {
            var pageLevel = -1;
            $.each($('div[id$=page]'), function(index, value) {
                var idVal = $(value).attr('id');
                if (idVal.indexOf('tab')==0) {
                    var stringLevel =  idVal.substring('tab'.length, idVal.length - '-page'.length)
                    var l = parseInt(stringLevel, 10);
                    if ((pageLevel == -1) || (l<pageLevel)) {
                        pageLevel = l;
                    }
                }
            });

            var from = $("#genPdfStart").val()-1;
            var to = $("#genPdfEnd").val()-1;


            var fromUuid = $($("#list-page>div.relItem")[from]).attr('pid');
            var toUuid = $($("#list-page>div.relItem")[to]).attr('pid');
            
            var u = "pdf?uuidFrom=" + fromUuid+"&uuidTo="+toUuid+"&path="+PDF.path(pageLevel-1);
            window.location.href = u;
        },
        // generovani pdf
        generatePDF:function(level) {
            //hideAdminOptions(level);
            PDF.openGeneratePdfDialog(level);
        },
		
        changeRange:function(odkud,kam) {
            $('#tv_container_row td').each(function(i, o){
                var index = i+1;
                if ((index >= odkud ) && (index<=kam)) {
                    if (!$(o).hasClass('tv_img_multiselect')) {
                        $(o).addClass('tv_img_multiselect');
                    }
                } else {
                    if ($(o).hasClass('tv_img_multiselect')) {
                        $(o).removeClass('tv_img_multiselect');
                    }
                }
            });
        },
		
        findRange:function() {
            var fromIndex = -1;
            var toIndex = -1;
            var pagesCount = $("#list-page>div.relItem").length;
            $('#tv_container_row div').each(function(i, o){
            	
            	if ($(o).hasClass('tv_img_selected')) {
                    fromIndex = i+1;
                    toIndex = fromIndex + generatePdfMaxRange-1;
                    if (toIndex > pagesCount) {
                        toIndex = pagesCount;
                    }
                }
                if ((fromIndex > 0) && (i < toIndex)) {
                    $(o).parent().toggleClass('tv_img_multiselect');
                }
                if ((i>= toIndex) && (toIndex != -1 )) return false;
            });
            return {
                fromIndex: fromIndex,
                toIndex: toIndex
            }
        },

		
        // dialog pro vyber stranek 
        openGeneratePdfDialog:function (level){
            PDF.privateLevel = level;
            var range = PDF.findRange();
            var pagesCount = $("#list-page>div.relItem").length;
		    
            $("#genPdfEnd").val(range.toIndex);
            $("#genPdfStart").val(range.fromIndex);

            if(PDF.dialogSummary){
                PDF.dialogSummary.dialog('open');
            }else{
		    	
                $.get("i18n?action=text&name=first_page_html", function(xml) {
                    var head = $(xml).find('head').text();
                    var body = $(xml).find('desc').text();
                    $("#pdf_desc_head").html(head);
                    $("#pdf_desc_content").html(body);
                });
		    	
		        
                $('#genPdfStart').change(function() {
                    var start = $("#genPdfStart").val();
                    var stop = $("#genPdfEnd").val();
                    PDF.changeRange(parseInt(start), parseInt(stop));
                });
                $('#genPdfEnd').change(function() {
                    var start = $("#genPdfStart").val();
                    var stop = $("#genPdfEnd").val();
                    PDF.changeRange(parseInt(start), parseInt(stop));
                });
		    	
                PDF.dialogSummary = $("#pdf_options").dialog({
                    bgiframe: true,
                    width: 400,
                    height: 300,
                    modal: true,
                    title: dictionary['generatePdfTitle'],
		            
                    buttons: {
                        "Ok": function() {
		        	
                            var from = $("#genPdfStart").val();
                            var to = $("#genPdfEnd").val();
		                   
                            if(isNaN(from) || isNaN(to)) {
                                alert(dictionary['generatePdfErrorText']);
                                return;
                            }
                            from = parseInt(from);
                            to = parseInt(to); 
                            if (to > (pagesCount)) {
                                to = pagesCount;
                                $("#genPdfEnd").val(to);
                            }
		                    
                            if(to - from + 1 > generatePdfMaxRange){
                                alert("Maximalne "+generatePdfMaxRange+"!");
                            }else if(to>pagesCount  || isNaN(from) || isNaN(to)) {
                                alert(dictionary['generatePdfErrorText']);
                            }else if(to==pagesCount && from == '1'){
                                PDF.urlFromDialog(PDF.privateLevel);
                                $(this).dialog("close");
                            }else{
                                PDF.urlFromDialog(PDF.privateLevel);
                                $(this).dialog("close");
                            }

                            // zruseni selekce
                            $('#tv_container_row td').each(function(i, o){
                                if ($(o).hasClass('tv_img_multiselect')) {
                                    $(o).toggleClass('tv_img_multiselect');
                                }
                            });
		                    
                        } ,
                        "Cancel": function() {
                            // zruseni selekce
                            $('#tv_container_row td').each(function(i, o){
                                if ($(o).hasClass('tv_img_multiselect')) {
                                    $(o).toggleClass('tv_img_multiselect');
                                }
                            });
                            $(this).dialog("close"); 
                        } 
                    } 
                });
            }
        }
    }
}();

$( ".selector" ).dialog( { buttons: { "Ok": function() { $(this).dialog("close"); } } } );


function downloadOriginal(level, model) {
	var uuid = $("#tabs_"+level).attr('pid');
	var url = "djvu?uuid="+uuid+"&outputFormat=RAW&page=0&asFile=true";
	window.location.href = url;
    hideAdminOptions(level);
}

function _showInfo(obj, tab, model){
    $(obj).toggleClass('op_info');
    $(tab + ">div>div[id=list-"+model+"]").toggle();
}

function _showList(obj, tab, model){
    var m = model;
    if(m.indexOf("-")>-1){
        m = m.split("-")[1];
    }
    if($(tab + ">div>div[id=info-"+m+"]").text()==""){
        $(tab + ">div>div[id=info-"+m+"]").html($(tab+">div>div[id=list-"+m+"]>div.selected").text());
    }
    var h = $(window).height() - $(tab).offset().top - $(tab).height();
    $(tab + ">div>div[id=list-"+m+"]").css('max-height', h);
    $(tab + ">div>div[id=list-"+m+"]").css('_height', 'expression(this.scrollHeight > '+h+'? "'+h+'px" : "auto" )');
    var w = $(tab + ">div>div[id=list-"+m+"]").parent().width() - 4;
    $(tab + ">div>div[id=list-"+m+"]").css('width', w);
    $(tab + ">div>div[id=list-"+m+"]").toggle();
    
    var selected = $(tab+">div>div[id=list-"+m+"]>div.selected");
    scrollElement($(selected).parent(), $(selected));
    $(obj).toggleClass('op_info');
}

function showMets(level, model) {
    hideAdminOptions(level);
    var pid = $("#tabs_"+level).attr('pid');
    window.open("mets?pid=uuid:"+pid, "_blank");
}

var _metadataDialog;
function showMainContent(level, model){
    var pid = $("#tabs_"+level).attr('pid');
   
    var titul = $("#tabs_"+level+">div>div[id=info-"+model+"]>div>ul").attr('title');
    var page = new PageQuery(window.location.search);
    var path = page.getValue("path");
    if(path=="") return;
    if(_metadataDialog){
        _metadataDialog.dialog('open');
    }else{
        $(document.body).append('<div id="metaDataDialog"><div id="metaData"></div></div>')
        _metadataDialog = $('#metaDataDialog').dialog({
            width:640,
            height:480,
            modal:true,
            title:titul,
            buttons: {
                "Close": function() {
                    $(this).dialog("close"); 
                } 
            } 
        });
    }
   
    $('#metaData').html(imgLoadingBig);
    var url = "inc/details/biblioToRdf.jsp?pid=uuid:"+pid+"&xsl=default.jsp&display=full&model="+model;
    $.get(url, function(data){
        $('#metaData').html(data);
    });
    //toggleAdminOptions(level, model);
}

var _searchInsideDialog;

function getPidPath(level, model){
    var i = level;
    var pid_path = "";
    var title_path = "";
    while(i>0){
        if(i==level){
          pid_path = $("#tabs_"+i).attr('pid');
        }else{
          pid_path = $("#tabs_"+i).attr('pid') + "/" + pid_path;
        }
        if($("#tabs_"+i).parent().parent()==0){
            i=0;
        }else{
            var idi = $("#tabs_"+i).parent().parent().attr('id');
            var modeli = $("#tabs_"+i).parent().attr('id');
            i = idi.substring(5);
        }
    }
    return pid_path;
}


function showSearchInside(level, model){
    if(model.indexOf("-")) model = model.substring(model.indexOf("-")+1);
    if(!$("#tab"+level+"-"+model).is(':visible')) return;
    var l = $("#tab"+level+"-"+model).offset().left - $(window).scrollLeft();
    var t = $("#tab"+level+"-"+model).offset().top - $(window).scrollTop();
    var w = $("#tab"+level+"-"+model).css('width');
    var titul = $("#tabs_"+level+">div>div[id=info-"+model+"]").html();
    var pid_path = getPidPath(level, model);
    /*
     var titlebar = '<input type="text" class=" ui-corner-all" id="insideQuery" size="30" value="'+dictionary['administrator.menu.searchinside']+'" /> <img width="12px" onclick="searchInside()" alt="search" class="searchInsideImg" src="img/lupa.png"></button>' ;
     */
    var inputs = '<div id="da-inputs"><input class="da_input" size="30" id="insideQuery"  type="text" value="'+dictionary['administrator.menu.searchinside']+'" />' +
        ' <a href="javascript:searchInside();" ><img align="top" src="img/lupa_orange.png" border="0" alt="search"  /></a>' +
        '</div>';

    if(_searchInsideDialog){
        $('#searchInsideDialog').dialog('option', 'position', [l,t]);
        $('#searchInsideDialog').dialog('option', 'width', w);
        _searchInsideDialog.dialog('open');
        $('#insideTitle').html(titul);
        var oldpid = $('#insidePid').val();
        if(oldpid != pid_path){
            $('#insidePid').val(pid_path);
            $('#searchInsideResults').html('');
        }
        
    }else{
        $(document.body).append('<div id="searchInsideDialog" class="searchInsideDialog"> <div id="searchInsideForm" class="searchInsideForm"><div>'+inputs+'<span id="insideTitle">'+titul+'</span></div><input type="hidden" id="insidePid" value="'+pid_path+'" /></div></div>')

        _searchInsideDialog = $('#searchInsideDialog').dialog({
            width:w,
            height:100,
            minHeight:100,
            position:[l,t],
            modal:false,
            dialogClass:'searchInsideDialog shadow10',
            title:dictionary['administrator.menu.searchinside']
        });
        $('#insideQuery').keyup(function(e) {
            if (e.keyCode == 13) {
                searchInside();
            }
        });
        $('#insideQuery').click(function(e) {
            if ($(this).val()==dictionary['administrator.menu.searchinside']) {
                $(this).val('');
                $(this).addClass('searching');
            }
                this.focus();
        });
    }
    //$("#tabs_" + level + ">div>div.menuOptions").hide();
}

function searchInside(start){

    offset = start ? start : 0;
    $('#searchInsideResults').html(imgLoadingBig);
    var q = $('#insideQuery').val();
    var pid = $('#insidePid').val();
    //var url = "searchXSL.jsp?q="+q+"&offset="+offset+"&xsl=insearch.xsl&collapsed=false&facet=false&fq=pid_path:"+pid+"*";
    var url = "searchInside.jsp?q="+q+"&offset="+offset+"&xsl=insearch.xsl&collapsed=false&facet=false&fq=pid_path:"+pid+"*";
    $.get(url, function(data){
        if($('#searchInsideResults').length==0){
            $('#searchInsideDialog').dialog('option', 'height', 480);
            $('#searchInsideDialog').css('height', 420);
            $('#searchInsideDialog').append('<div id="searchInsideResults"></div>');
        }
        $('#searchInsideResults').html(data);
    });
}

function loadItemMenu(params){
    var url = 'itemMenu.jsp?'+params;
    $.get(url, function(data){
        clearThumbs();
        $('#itemTree').html(data);
    });
}

function getPageTitle(pid){
    return $("#" + pid).text();
}

function toggleAdminOptions(level, div){
    postProcessContextMenu();
    $('#menu'+level+'-'+div).css('width', 230);
	var il = $('#menu'+level+'-'+div).parent().width() + $('#menu'+level+'-'+div).parent().offset().left - $('#menu'+level+'-'+div).width();
    $('#menu'+level+'-'+div).css('left', il);
    //$('#menu'+level+'-'+div).css('display', 'block');
    //$('#openmenu'+level+'-'+div).css('display', 'none');
    $('#menu'+level+'-'+div).toggle();
    $('#openmenu'+level+'-'+div).toggle();
}

function hideAdminOptions(level){
    //$("#tabs_" + level + ">div>div.menuOptions").hide();
    
}

function switchDisplay() {
    showFullImageAndStoreMaxLevel();
}


function onLoadPlainImage() {
	if (imageInitialized) {
		$("#plainImageImg").fadeIn();
	}
        if(viewerOptions.hasAlto){
            showAlto(viewerOptions.uuid, 'plainImageImg');
        }
}

function onLoadFullImage(){
        setFullImageDimension();
        if(viewerOptions.hasAlto){
            showAlto(viewerOptions.uuid, 'imgFullImage');
        }
}

function onLoadThumb(obj){
    //if(!$(this).hasClass('tv_img_inactive') || $(this).hasClass('tv_img_selected'))
        $(obj).parent().css('width', '');
}

function onLoadPDFImage() {}

var imageInitialized = false;
function showImage(viewerOptions) {
	// different view for pdf	
	if (viewerOptions.isContentPDF()) {
		displayPDFImageContent();
	} else {
	
            var tilesPrepared = viewerOptions.deepZoomGenerated || viewerOptions.imageServerConfigured;
            var deepZoomDisplay = ((viewerOptions.deepZoomCofigurationEnabled) && (tilesPrepared));
    	    if (deepZoomDisplay) {
		    if (viewer == null) {
		        init();
		    }
	            displaySeadragonContent();                  
	            viewer.openDzi("deepZoom/"+viewerOptions.uuid+"/");
	    } else {
	            displayImageContent();
	            $("#plainImageImg").fadeOut("slow", function () {
	                $("#plainImageImg").attr('src','fullThumb?uuid='+viewerOptions.uuid);
	            });
	    }
            
	}		
	imageInitialized = true;
}

function hideAlto(){
    $("#alto").html('');
    $("#alto").hide();
}

function showAlto(uuid, img){
    var q = $("#q").val();
    if($('#insideQuery').length>0) q =$('#insideQuery').val();
    if(q=="") return;

    var w = $('#'+img).width();
    var h = $('#'+img).height();
    var url = "metsalto.jsp?q="+q+"&w="+w+"&h="+h+"&uuid=" + uuid;
    $.get(url, function(data){
        if(data.trim()!=""){
            if($("#alto").length==0){
                $(document.body).append('<div id="alto" style="position:absolute;z-index:1003;overflow:hidden;" onclick="switchDisplay(viewerOptions)"></div>');
            }else{

            }
            positionAlto(img);
            $("#alto").html(data);
            $("#alto").show();
        }
    });
}

function positionAlto(img){
    var h = 0;
    h = $('#'+img).height();
    var t = $('#'+img).offset().top;
    if(img == 'imgFullImage'){
        h = $('#fullImageContainer').height();
        //t = t - $('#fullImageContainer').scrollTop;
    }
    var w = $('#'+img).width();
    var l = $('#'+img).offset().left;
    $("#alto").css('width', w);
    $("#alto").css('height', h);
    $("#alto").css('left', l);
    $("#alto").css('top', t);
}

function displaySecuredContent() {
	$("#loadingDeepZoomImage").hide();
	$("#plainImage").hide();
	$("#pdfImage").hide();
	$("#container").hide();

	$("#securityError").show();
}

function displayImageContent() {
	$("#loadingDeepZoomImage").hide();
	$("#pdfImage").hide();
	$("#container").hide();
	$("#securityError").hide();


	$("#plainImage").show();
}

function displaySeadragonContent() {
	$("#securityError").hide();
	$("#pdfImage").hide();
	$("#plainImage").hide();
	$("#loadingDeepZoomImage").hide();

	$("#container").show();
}
function displayLoadingImageContent() {
	$("#securityError").hide();
	$("#pdfImage").hide();
	$("#plainImage").hide();
	$("#container").hide();

	$("#loadingDeepZoomImage").show();
}

function displayPDFImageContent() {
	$("#securityError").hide();
	$("#plainImage").hide();
	$("#container").hide();
	$("#loadingDeepZoomImage").hide();

	$("#pdfImage").show();

}

function showBornDigitalPDF(uuid,page) {
	if  (!page) {
		page = "1";	
	}
	var url = "djvu?uuid="+uuid+"&outputFormat=RAW#page="+page;
	var pdfWindow = window.open(url, '_blank');
	pdfWindow.focus();
}
