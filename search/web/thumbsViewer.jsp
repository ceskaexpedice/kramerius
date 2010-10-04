<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>

<%@page import="cz.incad.Kramerius.ThumbnailImageServlet"%>

<script type="text/javascript">
	var currentSelectedPageRights;

	var currentSelectedPage;
    var currentSelectedParent;
    var arrowsW = 70;
    var loadedImages = 0;
    var totalThumbs = 0;
    var currentMime = "unknown";
    
    var imgW;
    function _selectPage(uuid){
        $('.tv_image').removeClass('tv_img_selected');
        currentSelectedPage = uuid;
        var level = getMaxLevel()
		setSelection(level,"page",uuid);

        $("#tabs_"+getMaxLevel()).attr('pid', currentSelectedPage);

        var pageUrl = "djvu?uuid="+uuid+"&scaledWidth="+imgW;
        var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
        
        var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" alt="" width="'+imgW+'px" border="0" onerror="showError();"  /></a>';
        checkArrows();

//        if (viewer != null) {
//			showDeepZoomFile(uuid);
//        }
        
        $.get(mimeUrl, function(data){
            currentMime = data;
        });
        
        $('#img'+getMaxLevel()+'_'+uuid).toggleClass('tv_img_selected');
        changeSelectedPage(uuid);
    }
    function showError(){
        //$('#mainContent').html('<div align="center" style="height:300px;" >' + dictionary['rightMsg'] + '</div>');
        $('#imgBig').attr('alt', dictionary['rightMsg']);
    }
    
    var fullDialog;
    var vertMargin = 20;
    var horMargin = 17;
    var fullImageWidth;
    var fullImageHeight;
    var maxScroll = 0;
    function showFullImage(uuid){
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
                close: function(event, ui) {
                    $('#mainItemTable').show();  
                    $('#imgContainer>img').attr('src', 'img/empty.gif');
                    changeSelection(currentSelectedParent, currentSelectedPage);
                }

            });
            $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:previousFull();" class=" ui-corner-all ui-dialog-titlebar-prev prevArrow"><span class="ui-icon ui-icon-arrowthick-1-w">prev</span></a>');
            $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:nextFull();" class=" ui-corner-all ui-dialog-titlebar-next nextArrow"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>');
            if(currentMime.indexOf('djvu') == -1){
                $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append($('#divFullImageZoom').html());
            }
            checkArrows();
        }
        //alert(currentMime);
        var fullUrl = "djvu?uuid="+currentSelectedPage+"&outputFormat=RAW";
        if(currentMime.indexOf('djvu') > 0){
            //$('#djvuContainer>object>param[name="src"]').attr('value', fullUrl);
            //$('#djvuContainer>object>embed').attr('src', fullUrl);
            
            $('#djvuContainer>iframe').attr('src', fullUrl);
            $('#djvuContainer').show();
            
        }else if(currentMime.indexOf('pdf') > 0){
            fullUrl = fullUrl + "#page=" + $('#pdfContainer>input').val();
            //alert(fullUrl);
            $('#pdfContainer>iframe').attr('src', fullUrl);
            $('#pdfContainer').show();
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
        selectPrevious();
        showFullImage();
    }
    function nextFull(){
        selectNext();
        showFullImage();
    }
    
    function checkScrollPosition(){
        loadedImages++;
        if(loadedImages == totalThumbs){
            setInactiveImagesWidth();
            var to = $('#img_' + currentSelectedPage).offset().left - getImgContainerLeft();
            to = to / $("#tv_container_table").width()  * 100;
            slideTo(to);
        }
    }
    function changeSelection(masterUuid, selection) {
        currentSelectedParent = masterUuid;
        var to = $('#img'+getMaxLevel()+'_' + selection).offset().left - getImgContainerLeft() + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
        var maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        var to2 = 0;
        if(maxScroll > 0){
            to2 = to * 100 / maxScroll;
        }
       
        canScroll = false;
        slideTo(to2, selection);
        selectPage(selection);
        $("#tv_container").attr("scrollLeft", to);
        canScroll = true;
    }
     var tvContainerRight;
     var tvContainerLeft;
    function slideTo(pos){
        canScroll = false;
        maxScroll = $("#tv_container").attr("scrollWidth") - getImgContainerWidth();
        $("#tv_slider").slider("value", pos);
        $("#tv_container").attr({
            scrollLeft: pos * (maxScroll / 100)
        });
        canScroll = true;
        
        //testujeme jestli obrazek je cely videt
        
        //var r = $('#img_' + selection).offset().left + $('#img_' + selection).width();
        //if(r>tvContainerRight){
        //    var pos2 = r - tvContainerLeft;
        //    slideTo(pos2, selection);
        //}
    }
    
    
        
    var sliderCreated = false;
    
    var canScroll = true;
    function tv_SliderChange(e, ui){
        
        //if(maxScroll==0){
        //    maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        //}
        if(canScroll){
            maxScroll = $("#tv_container").attr("scrollWidth") - getImgContainerWidth();
            $("#tv_container").attr({
                scrollLeft: ui.value * (maxScroll / 100)
            });
            activateThumbs();
        }
    }
        
    function tv_left() {
        var value = $("#tv_slider").slider( "option", "value" );
        if (value > 0) {
            $("#tv_slider").slider( "option", "value", value-1 );
            onSlider($("#tv_slider").slider( "option", "value"));
        }
    }

    function tv_right() {
        var slider = $("#tv_slider");
        var value = slider.slider( "option", "value" );
        var max = slider.slider( "option", "max" );
        if (value < max) {
            $("#tv_slider").slider( "option", "value", value+1 );
            onSlider($("#tv_slider").slider( "option", "value"));
        }
    }
    
    $(document).ready(function(){
        //imgW = $('#mainContent').width();
        imgW = 650;
        tvContainerLeft = $("#tv_container").offset().left;
        tvContainerRight = tvContainerLeft + $("#tv_container").width();
        $("#tv_slider").slider({
            animate: false,
            step: 1,
            change: tv_SliderChange,
            slide: tv_SliderChange
        });
        
        $(document).keyup(function(e) {
            if (e.keyCode == 39) {
                selectNext();
            } else if (e.keyCode == 37) {
                selectPrevious();
            }
        });
        

    });
    
    function resizeFullImage(){
        $('#fullImageContainer').dialog('option', {
                left:0, 
                top:0, 
                height:$(window).height()-vertMargin, 
                width:$(window).width()-horMargin
        });
    }
    var resizeTimer = null;
    $(window).bind('resize', function() {
        
            if (resizeTimer) clearTimeout(resizeTimer);
            resizeTimer = setTimeout(resizeFullImage, 100);
        
    }); 
</script>
<style>
    #tv_container{
        overflow: hidden;
        width:900px;
        height:150px;
    }
    .tv_image{
        padding:2px;
        position:relative;
    }
    .tv_img_selected{
        border:solid 2px #e75c01;
        padding:0;
    }
    .tv_img_multiselect {
        border-bottom:solid gray;
    }
    .tv_img_inactive{
        height:128px;
        background:white  url(img/loading2.gif) no-repeat 50% 50%;
        
    }
    #tv_container tr td div{
        height:130px;
        width:128px;
        border:solid gray 1px;
        overflow:hidden;
        background:white url(img/background.png) repeat-x;
    }
    
</style>
<div id="tv">
<div id="tv_container">
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
<table cellpadding="2" cellspacing="0" width="100%">
    <tr><td><div id="tv_slider" style="width: 100%"></div> </td></tr>
</table>
</div>
