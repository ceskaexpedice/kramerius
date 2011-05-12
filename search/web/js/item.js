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

/** bind arrow keys*/
function bindArrows() {
    // keys - bind left and right arrows
	$(document).keyup(function(e) {
        if (e.keyCode == 39) {
            selectNext();
        } else if (e.keyCode == 37) {
            selectPrevious();
        }
    });
}

/** unbind arrow keys*/
function unbindArrows() {
	$(document).unbind('keyup');
}



function hideContextMenu(){
    $('.menuOptions').each(function(index){
       var id = $(this).attr('id');
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
function showMetadata(level, model){
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
    //var url = "inc/details/biblioToRdf.jsp?pid=uuid:"+pid+"&xsl=default.jsp&display=full&model="+model;
    var url = "inc/details/metadataFull.jsp?pid="+pid+"&model="+model;
    $.get(url, function(data){
        $('#metaData').html(data);
        $('#mods-full').tabs();
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
    //$("#tabs_" + level + ">div>div.menuOptions").hide(); //??
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
	if (viewerOptions.isContentPDF()) {
		displayImageContainer("#pdfImage");
		if (viewerOptions.previewStreamGenerated) {
			$("#pdfImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
		} else {
			$("#pdfImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
		}
	} else {
            var tilesPrepared = viewerOptions.deepZoomGenerated || viewerOptions.imageServerConfigured;
            var deepZoomDisplay = ((viewerOptions.deepZoomCofigurationEnabled) && (tilesPrepared));
    	    if (deepZoomDisplay) {
		    if (viewer == null) {
		        init();
		    }
	    		displayImageContainer("#container");
	            viewer.openDzi("deepZoom/"+viewerOptions.uuid+"/");
	    } else {
	            displayImageContainer("#plainImage");
	            
	            $("#plainImageImg").fadeOut("slow", function () {
	                // http://code.google.com/p/kramerius/issues/detail?id=43
	    			$("#plainImageImg").attr('src','img/empty.gif');
	    			
	    			// XXX: Changed from thumb servlet to img servlet
	    			// previous -> $("#plainImageImg").attr('src','fullThumb?uuid='+viewerOptions.uuid);
	    			if (viewerOptions.previewStreamGenerated) {
		    			$("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_PREVIEW&action=GETRAW');
	    			} else {
	    				// this should be directed by property or removed
		    			$("#plainImageImg").attr('src','img?uuid='+viewerOptions.uuid+'&stream=IMG_FULL&action=SCALE&scaledHeight=700');
	    			}
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

function displayImageContainer(contentToShow) {
	$.each([
        "#loadingDeepZoomImage", 
        "#plainImage",
	 	"#pdfImage",
	 	"#container",
	 	"#noImageError",
	 	"#securityError"],
	 	
	 	function(index,item) {
			if (item==contentToShow) {
				$(item).show();
			} else {
				$(item).hide();
			}
		}
	);
}

function showBornDigitalPDF(uuid,page) {
	if  (!page) {
		page = "1";	
	}
	var url ='img?uuid='+uuid+'&stream=IMG_FULL&action=GETRAW#page='+page;
	var pdfWindow = window.open(url, '_blank');
	pdfWindow.focus();
}
