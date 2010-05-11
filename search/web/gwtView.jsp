<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%><script type="text/javascript" language="javascript" src="gwtviewers/gwtviewers.nocache.js"></script>
<script type="text/javascript">

	var __gwtViewersUUIDPATH = "${empty param.pid_path ? param.pid : param.pid_path }";
	var __confHeight = 140;
	var __confWidth = 400;
	var __confDistance = 10;
	var __confNumberOfImages = 10;	
	var __confMoveStep = 1;
	var __debug = false;
	
	var currentSelectedPage;
	var currentSelectedParent;
        
	function changeSelection(masterUuid, selection) {
          currentSelectedPage = selection;
            currentSelectedParent = masterUuid;
		requestToSelect(masterUuid, selection);
        }
        
        // callbacks from component
        
        var prev = '<span style="padding:15px;cursor:pointer;" onclick="selectPrevious();"><img src="img/la.png" /></span>';
        var next = '<span style="padding:15px;cursor:pointer;" onclick="selectNext();"><img src="img/ra.png" /></span>';
        var currentMime = "unknown";
	function selectPage(uuid, mimetype){
            currentSelectedPage = uuid;
            var pageUrl = "djvu?uuid="+uuid+"&scaledHeight=600";
            var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
            //var img = '<a href="javascript:showImageFull(\''+fullUrl+'\', \''+ mimetype +'\'"><img src="'+pageUrl+'" height="600px" /></a>';
            var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" height="600px" border="0"  /></a>';
            //var img = '<a class="lighbox" href="'+fullUrl+'" target="_blank" ><img id="imgBig" src="'+pageUrl+'" height="600px" border="0" mimetype="unknown" /></a>';
            $('#mainContent').html('<div align="center" style="">' + prev + img + next + '</div>');
            //$('.lighbox').lightBox();
            
            $.get(mimeUrl, function(data){
                currentMime = data;
            });
            changeSelectedPage(uuid);
	}
        var fullDialog;
        var vertMargin = 20;
        var horMargin = 17;
        var fullImageWidth;
        var fullImageHeight;
        function showFullImage(uuid){
            
            var fullUrl = "djvu?uuid="+currentSelectedPage+"&outputFormat=RAW";
            $('#mainItemTable').hide();
            //return;
            if(fullDialog){
                fullDialog.dialog("option","title", getPageTitle(currentSelectedPage));
                fullDialog.dialog("option","height", $(window).height()-vertMargin);
                fullDialog.dialog("option","width", $(window).width()-horMargin);
                fullDialog.dialog('open');
            } else {
                fullDialog = $('#fullImageContainer').dialog({
                    left:0, 
                    top:0, 
                    height:$(window).height()-vertMargin, 
                    width:$(window).width()-horMargin,
                    modal:true,
                    resizable:false,
                    draggable:false,
                    title:getPageTitle(currentSelectedPage),
                    close: function(event, ui) {$('#mainItemTable').show();  $('#imgContainer>img').attr('src', 'img/empty.gif');}

                });
                $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:previousFull();" class=" ui-corner-all ui-dialog-titlebar-prev"><span class="ui-icon ui-icon-arrowthick-1-w">prev</span></a>');
                $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:nextFull();" class=" ui-corner-all ui-dialog-titlebar-next"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>');
                if(currentMime!= 'image/djvu'){
                   $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append($('#divFullImageZoom').html());
                }
            }
            //alert(currentMime);
            if(currentMime== 'image/djvu'){
                //$('#djvuContainer').hide();
                $('#djvuContainer>object>param[name="src"]').attr('value', fullUrl);
                $('#djvuContainer>object>embed').attr('src', fullUrl);
                $('#djvuContainer').show();
            }else{
                $('#imgContainer').show();
                $('#imgContainer>img').attr('src', fullUrl);
            }
        }
        function changeFullImageZoom(){
        //alert($('#fullImageZoom').val());
            var zoom = $('#fullImageZoom').val();
            if(zoom=="width"){
                $('#imgContainer>img').css({'width': $('#imgContainer').width(), 'height': ''});
                
            }else if(zoom=="height"){
                //var w = 
                $('#imgContainer>img').css({'height': $(window).height()-vertMargin-$('.ui-dialog-titlebar').height()-5,
                    'width': ''});
            }else{
                var w = Math.round(document.getElementById('imgFullImage').naturalWidth * parseFloat(zoom));
                var h = Math.round(document.getElementById('imgFullImage').naturalHeight * parseFloat(zoom));
                $('#imgContainer>img').css({'width': w, 'height': h});
                //$('#imgContainer>img').css('height', $('#fullImageZoom').val());
            }
        }
        function previousFull(){
            //$('#imgContainer>img').attr('src', 'img/loading.gif').css({'width': '', 'height': 'h'});
            selectPrevious();
            //setTimeout('showFullImage()', 100);
            showFullImage();
        }
        function nextFull(){
            //$('#imgContainer>img').attr('src', 'img/loading.gif').css({'width': '', 'height': 'h'});
            selectNext();
            //setTimeout('showFullImage()', 100);
            showFullImage();
        }
	
   	/*--- callback from component - page range changed ---*/
	function onChangePages(from, to){  
		var previous = $("#range").html();
		$("#range").html(""+(from+1)+" - "+to);	
	}
   	/*--- end of callback from component ---*/
	
	
	/*--- JQuery slider ---*/
	function createSlider(mmin, mmax, mcur, width) {
   		$("#slider").slider({
			max: mmax,
			min:mmin,
			value:mcur
		});
		
		$("#slider").css("width",width);
		$("#slider").bind( "slide", function(event, ui) {
			showPages();
			jQuerySliderChange(ui.value);
		});

		$("#slider").bind("slidestop", function(event, ui) {
			hidePages();
			jQuerySliderMouseUp();
		});
	}

	function getSliderValue() {
		var vl = $("#slider").slider("value");
		return vl;
	}
	
	function setSliderValue(value) {
		$("#slider").slider("value", value);
		jQuerySliderChange(value);
	}
	/*--- end of JQuery slider ---*/

	
	/*--- pages range dialog ---*/
	var pagesWindow = null;
	function hidePages() {
		if (pagesWindow) {
			pagesWindow.dialog("close");
		}
	}

	function showPages() {
		//alert("Test");
		if (pagesWindow) {
			pagesWindow.dialog("open");
		} else {
			pagesWindow =  $("#pages").dialog({
		        bgiframe: false,
		        width: 350,
		        height: 60,
		        minHeight:60,
		        modal: false,
		        draggable:false,
		        resizable:false,
		        title:"rozsah stránek"
		    });
		}
	}
	/*--- end of pages range dialog ---*/
	
</script>
 
<table align="center" width="100%">
	<tr>
            <td colspan="3" id="label"></td>
	</tr>
	<tr>
            <td id="container"></td>
	</tr>
	<tr>
    	<td><div id="slider" style="width: 100%"></div> </td>
	</tr>
</table>

<div id="pages" style="display: none">
	<div id="range" style="text-align: center;"></div>
</div>
