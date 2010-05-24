<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>

<%@page import="cz.incad.Kramerius.ThumbnailImageServlet"%>
<script type="text/javascript" language="javascript" src="gwtn/gwtn.nocache.js"></script>

<script type="text/javascript">

	var __confNumberOfImages = 8;	

	var currentSelectedPage;
	var currentSelectedParent;

    
	function changeSelection(masterUuid, selection) {
		if ((currentSelectedParent != null) && (currentSelectedParent != masterUuid)) {
			//znovu nacteni 
			initialize();
		}
		currentSelectedPage = selection;
		currentSelectedParent = masterUuid;

		// momentalne zobrazeny 
                var currentLeft = $("#slider").slider("option", "value");
		var currentRight = currentLeft  + __confNumberOfImages;
		var ind = index(selection);
		if ((ind > currentRight) || (ind < currentLeft)) {
	    	$("#slider").slider( "option", "value", ind-1);
			onSlider($("#slider").slider( "option", "value"));
		}
		
		// zmena selekce
	
		select(selection);
     }
        
        // callbacks from component
        
        var prev = '<span style="padding:15px;cursor:pointer;" onclick="selectPrevious();"><img src="img/la.png" /></span>';
        var next = '<span style="padding:15px;cursor:pointer;" onclick="selectNext();"><img src="img/ra.png" /></span>';
        var currentMime = "unknown";

        function selectPage(uuid, mimetype){
            currentSelectedPage = uuid;
            var pageUrl = "djvu?uuid="+uuid+"&scaledHeight=600";
            var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
            var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" height="600px" border="0" onerror="showError();"  /></a>';
            $('#mainContent').html('<div align="center" style="">' + prev + img + next + '</div>');
            
            $.get(mimeUrl, function(data){
                currentMime = data;
            });
            changeSelectedPage(uuid);
	}
        
        function showError(){
            $('#mainContent').html('<div align="center" style="height:300px;" >' + dictionary['rightMsg'] + '</div>');
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
                if(currentMime.indexOf('djvu') == -1){
                   $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append($('#divFullImageZoom').html());
                }
            }
            //alert(currentMime);
            if(currentMime.indexOf('djvu') > 0){
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
	

    /*-- found level where pages are placed --*/
	function maxLevel() {
		var pocitadlo = 1;
		var exists = $('#tabs_'+pocitadlo);
		while((exists) && (exists.length != 0)) {
			pocitadlo = pocitadlo + 1;
			exists = $('#tabs_'+pocitadlo);
		}
		return (pocitadlo-1);							
	}
    
	/*-- found all pages --*/
	function findPages() {
		var pages = [];
		var max = maxLevel();
		var divs =  $('#tab'+max+'-page>div#list-page>div');
		$.each(divs, function(i,item){
			pages[i] = ''+item.attributes['id'].value;
		});
		return pages;		
	}
    
	/*-- move left --*/
    function left() {
    	var value = $("#slider").slider( "option", "value" );
		if (value > 0) {
	    	$("#slider").slider( "option", "value", value-1 );
			onSlider($("#slider").slider( "option", "value"));
    	}
    }

	/*-- move left --*/
    function right() {
        var slider = $("#slider");
    	var value = slider.slider( "option", "value" );
		var max = slider.slider( "option", "max" );
    	if (value < max) {
	    	$("#slider").slider( "option", "value", value+1 );
			onSlider($("#slider").slider( "option", "value"));
    	}
	}

    var sliderCreated = false;
    /*--- JQuery slider ---*/
	function createSlider(mmin, mmax, mcur) {
		if (sliderCreated) {
			$("#slider").slider( "option", "max", mmax );
			$("#slider").slider( "option", "value", mcur );
					
		} else {
	    	$("#slider").slider({
				max: mmax,
				min:mmin,
				value:mcur
			});

	    	
			$("#slider").css("width",getImgContainerWidth());
			$("#slider").bind( "slide", function(event, ui) {
				onSlider(ui.value);
			});

			$("#slider").bind("slidestop", function(event, ui) {
				onSlider(ui.value);
			});

			sliderCreated = true;
		}			
		
	}

	/*-- Get url of images --*/
    function getImageURL() {
        return '<%= ThumbnailImageServlet.thumbImageServlet(request) %>?outputFormat=RAW&uuid='
	}
	

	function getImgContainerWidth() {
		return "900px";	
	}


	$(document).keypress(function(e) {
		if (e.keyCode == 39) {
            selectNext();
		} else if (e.keyCode == 37) {
            selectPrevious();
		}
	});
	
		
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
