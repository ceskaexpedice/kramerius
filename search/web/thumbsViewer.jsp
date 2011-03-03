<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>


<script type="text/javascript">
	var currentSelectedPageRights;

	var currentSelectedPage;
    var currentSelectedParent;
    var arrowsW = 70;
    var loadedImages = 0;
    var totalThumbs = 0;
 
    // viewer options for display content
    var viewerOptions = { deepZoomGenerated:false, 
                          deepZoomCofigurationEnabled:false, 
                          mimeType:'', 
                          status:200,
                          uuid:'',
                          isContentPDF:function() {return viewerOptions.mimeType=='application/pdf'},
                          isContentDJVU:function() { return viewerOptions.mimeType.indexOf('djvu')> 0 }
    };
    
    var maxLevelForFullImageShow = -1;
    var selectedListForFullImageShow = "";
                          
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

    var fullImageWidth;
    var fullImageHeight;
    function setFullImageDimension(){
         var newImg = new Image();
         newImg.src = $('#imgContainer>img').attr('src');
         fullImageWidth = newImg.width;
         fullImageHeight = newImg.height;
    }

    function showFullImage() {
        $('#mainItemTable').hide();
        //return;
        if(fullDialog){
            fullDialog.dialog("option","title", getPageTitle(currentSelectedPage));
            fullDialog.dialog("option","height", $(window).height()-vertMargin-12);
            fullDialog.dialog("option","width", $(window).width()-horMargin-12);
            fullDialog.dialog('open');
        } else {
            fullDialog = $('#fullImageContainer').dialog({
                left:0, 
                top:0, 
                height:$(window).height()-vertMargin-12,
                width:$(window).width()-horMargin-12,
                modal:true,
                resizable:false,
                draggable:false,
                title:getPageTitle(currentSelectedPage),
                close: function(event, ui) {
                    maxLevelForFullImageShow = -1;
                    $('#mainItemTable').show();  
                    $('#imgContainer>img').attr('src', 'img/empty.gif');
                    selectPage(currentSelectedPage);
                }
            });
            $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:previousFull();" class=" ui-corner-all ui-dialog-titlebar-prev prevArrow"><span class="ui-icon ui-icon-arrowthick-1-w">prev</span></a>');
            $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append('<a href="javascript:nextFull();" class=" ui-corner-all ui-dialog-titlebar-next nextArrow"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></a>');
            
            if (!viewerOptions.isContentDJVU()) {
                $('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').append($('#divFullImageZoom').html());
            } else {

//                alert($('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').html());
                //$('[aria-labelledby=ui-dialog-title-fullImageContainer]>.ui-dialog-titlebar').empty();
            }
            
            checkArrows();
        }
        var fullUrl = "djvu?uuid="+viewerOptions.uuid+"&outputFormat=RAW";
        if(viewerOptions.isContentDJVU()){
            //$('#djvuContainer>object>param[name="src"]').attr('value', fullUrl);
            //$('#djvuContainer>object>embed').attr('src', fullUrl);
            
            $('#djvuContainer>iframe').attr('src', fullUrl);
            $('#djvuContainer').show();
            
        }else if(viewerOptions.isContentPDF()){
            fullUrl = fullUrl + "#page=" + $('#pdfContainer>input').val();
            $('#pdfContainer>iframe').attr('src', fullUrl);
            $('#pdfContainer').show();
        }else{
            $('#imgContainer').show();
            $('#imgContainer>img').attr('src', fullUrl);
        }

        $('#fullImageContainer').scroll(function(){
            if(viewerOptions.hasAlto){
                positionAlto('imgFullImage');
            }
            
        });
    }

    function showFullImageAndStoreMaxLevel(){
        maxLevelForFullImageShow=getMaxLevel();
        selectedListForFullImageShow = $('#tabs_'+maxLevelForFullImageShow+'>div:visible').attr("id");
        showFullImage();
    }


    function changeFullImageZoom(){
        var zoom = $('#fullImageZoom').val();
        
        if(zoom=="width"){
            $('#imgContainer>img').css({'width': $('#imgContainer').width(), 'height': ''});
                
        }else if(zoom=="height"){
            //var w = 
            $('#imgContainer>img').css({'height': $(window).height()-vertMargin-$('.ui-dialog-titlebar').height()-5,
                'width': ''});
        }else{
            //var w = Math.round(document.getElementById('imgFullImage').naturalWidth * parseFloat(zoom));
            //var h = Math.round(document.getElementById('imgFullImage').naturalHeight * parseFloat(zoom));
            var w = Math.round(fullImageWidth * parseFloat(zoom));
            var h = Math.round(fullImageHeight * parseFloat(zoom));
            $('#imgContainer>img').css({'width': w, 'height': h});
            
            //$('#imgContainer>img').attr('height', zoom+ '% ');
            //$('#imgContainer>img').css('height', $('#fullImageZoom').val());
        }
        if(viewerOptions.hasAlto){
            showAlto(viewerOptions.uuid, 'imgFullImage');
        }
    }
    function previousFull(){
        selectPrevious();
        //showFullImage();
        getViewInfo(currentSelectedPage, showFullImage);
    }
    function nextFull(){
        selectNext();
        getViewInfo(currentSelectedPage, showFullImage);
        //showFullImage();
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
        /*
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
        */
    }
    
    
     var tvContainerRight;
     var tvContainerLeft;
    function slideTo(pos){
        canScroll = false;
        maxScroll = $("#tv_container").attr("scrollWidth") - getTvContainerWidth();
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
            maxScroll = $("#tv_container").attr("scrollWidth") - getTvContainerWidth();
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
    
    function setFullDialogSize(){
        if(fullDialog){
            fullDialog.dialog("option","height", $(window).height()-vertMargin-12);
            fullDialog.dialog("option","width", $(window).width()-horMargin-12);
        }
    }
    
    function resizeElements(){
        setFullDialogSize();
        setTvContainerWidth();
        activateThumbs();
        if($("#alto").length>0){
            
            if($("#imgFullImage").is(':visible')){
                positionAlto("imgFullImage");
            }else{
                positionAlto("plainImageImg");
                    
            }
        } 
    }
    var resizeTimer = null;
    $(window).bind('resize', function() {
            if (resizeTimer) clearTimeout(resizeTimer);
            resizeTimer = setTimeout(resizeElements, 100);
    }); 
</script>
<style>
    #tv{
        width: 100%;
    }
    #tv_container{
        overflow: hidden;
        position:relative;
        height:150px;
    }
    .tv_image{
        margin:2px;
        position:relative;
        border:1px solid silver;
    }
    .tv_img_multiselect {
        border-bottom:solid gray;
    }
    .tv_img_inactive{
        height:128px;
        background:white  url(img/loading.gif) no-repeat 50% 50%;
        
    }
    #tv_container tr td div{
        height:134px;
        /*width:128px;*/
        border:solid white 1px;
        overflow:hidden;
        background:#F1F1F1;
        margin: 0px 4px;



    -moz-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
    -webkit-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
    box-shadow:0 0 6px rgba(0, 0, 0, 0.5);


    }
    #tv_container tr td div.tv_img_selected{
        border:solid 2px #e75c01;
        padding:0;
    }
    #tv_container tr td div img{
        height:128px;
    }

    #tv_scroll_table{
        padding-left:18px;
        padding-rigth:18px;
    }
    
</style>
<div id="tv">
<div id="tv_container">
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
<table cellpadding="2" cellspacing="0" width="100%" id="tv_scroll_table">
    <tr><td><div id="tv_slider" style="width: 100%"></div> </td></tr>
</table>
</div>
