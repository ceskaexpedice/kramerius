<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>

<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>

<%@page import="cz.incad.Kramerius.ThumbnailImageServlet"%>

<script type="text/javascript">
    var currentSelectedPage;
    var currentSelectedParent;
    var arrowsW = 70;
    var loadedImages = 0;
    var totalThumbs = 0;
    var currentMime = "unknown";
        
        
    function addThumb(uuid){
        var img = '<td><img onload="checkScrollPosition()" id="img_'+uuid+
            '" class="tv_image';
        if(currentSelectedPage==uuid){
            img += ' tv_img_selected'
        }
        img += '" onclick="selectPage(\''+uuid+'\');" src="thumb?outputFormat=RAW&amp;uuid='+uuid+'" /></td>';
        $('#tv_container_row').append(img);
        checkArrows();
    }
    
    function clearThumbs(){
        $('#tv_container_row>td').remove();
    }
    var imgW;
    function selectPage(uuid){
        $("#img_"+currentSelectedPage).toggleClass('tv_img_selected');
        currentSelectedPage = uuid;
        var pageUrl = "djvu?uuid="+uuid+"&scaledHeight=600";
        var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
        var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" width="'+imgW+'px" border="0" onerror="showError();"  /></a>';
        checkArrows();
        $('#mainContent').html(img);

        $.get(mimeUrl, function(data){
            currentMime = data;
        });
        $("#img_"+uuid).toggleClass('tv_img_selected');
        changeSelectedPage(uuid);
    }
    function checkArrows(){
        var obj = $('#' + currentSelectedPage).prev();
        if($(obj).length>0){
            $('.prevArrow').show();
        }else{
            $('.prevArrow').hide();
        }
        obj = $('#' + currentSelectedPage).next();
        if($(obj).length>0){
            $('.nextArrow').show();
        }else{
            $('.nextArrow').hide();
        }
    }
    function showError(){
        $('#mainContent').html('<div align="center" style="height:300px;" >' + dictionary['rightMsg'] + '</div>');
    }
    var fullDialog;
    var vertMargin = 20;
    var horMargin = 17;
    var fullImageWidth;
    var fullImageHeight;
    var maxScroll = 0;
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
        selectPrevious();
        showFullImage();
    }
    function nextFull(){
        selectNext();
        showFullImage();
    }
    
    function checkScrollPosition(){
        loadedImages++;
        //alert(totalThumbs);
        if(loadedImages == totalThumbs){
        //alert(loadedImages);
            var to = $('#img_' + currentSelectedPage).offset().left - tvContainerLeft;
            to = to / $("#tv_container_table").width()  * 100;
            slideTo(to, currentSelectedPage);
        }
    }
    function changeSelection(masterUuid, selection) {
        currentSelectedParent = masterUuid;
        
        // momentalne zobrazeny 
        var to = $('#img_' + selection).offset().left - tvContainerLeft;
        to = to / $("#tv_container_table").width()  * 100;
        slideTo(to, selection);
        selectPage(selection);
    }
     var tvContainerRight;
     var tvContainerLeft;
    function slideTo(pos, selection){
        $("#tv_slider").slider("value", pos);
        
        //testujeme jestli obrazek je cely videt
        var r = $('#img_' + selection).offset().left + $('#img_' + selection).width();
        if(r>tvContainerRight){
            var pos2 = r - tvContainerLeft;
            slideTo(pos2, selection);
        }
    }
    function getImgContainerWidth() {
        return "900px";	
    }
        
    var sliderCreated = false;
    
    
    function tv_SliderChange(e, ui){
        if(maxScroll==0){
            maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        }
        $("#tv_container").attr({
            scrollLeft: ui.value * (maxScroll / 100)
        });
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
        imgW = $('#mainContent').width();
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
    
</style>
<div id="tv_container">
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
<table cellpadding="2" cellspacing="0" width="100%">
    <tr><td><div id="tv_slider" style="width: 100%"></div> </td></tr>
</table>
