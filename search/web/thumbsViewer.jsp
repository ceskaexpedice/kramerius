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
        
    function getMaxLevel(){
        var maxLevel = 1;
        var id;
        var cur;
        //alert($('.ui-tabs').length);
        $('.ui-tabs').each(function(index){
            id = $(this).attr('id');
            cur = parseInt(id.substr(5));
            if($('#'+id).is(':visible') && cur>maxLevel){
                maxLevel = cur;
            }
        });
        return maxLevel;
    }
    function updateThumbs(){
        //alert(currentSelectedPage);
        //alert(currentSelectedParent);
        if(changingTab){
            initPage = null;
            var maxLevel = getMaxLevel();
            $('.thumb').hide();
            $('.inlevel_'+maxLevel).show();
            if($('#img'+maxLevel+'_'+currentSelectedPage).is(':visible')){
                changeSelection(currentSelectedParent, currentSelectedPage);
            }else{
                var d1 = "#tabs_" + maxLevel;
                var d2 = "#tabs_" + (maxLevel-1);
                var pid = $(d1+">div.page>div.relList>div:first").attr("pid");
                changeSelection($(d2).attr("pid"),pid);
                //showInfo($(d1+">ul>li>img"), d1, 'page');
            }
            checkArrows();
        }
    }
    
    function addThumb(uuid, display, level){
        var img = '<td style="display:'+display+';" class="thumb inlevel_'+level+'"><img onload="checkScrollPosition()" id="img'+level+'_'+uuid+
            '" class="tv_image';
        if(currentSelectedPage==uuid){
            img += ' tv_img_selected'
        }
        img += '" onclick="selectPage(\''+uuid+'\');" src="thumb?outputFormat=RAW&amp;uuid='+uuid+'" /></td>';
        $('#tv_container_row').append(img);
        if(totalThumbs==0){
            $('#tv').hide();
        }else{
            $('#tv').show();
        }
        checkArrows();
    }
    
    function clearThumbs(){
        $('#tv_container_row>td').remove();
        totalThumbs = 0;
    }
    var imgW;
    function selectPage(uuid){
        $('.tv_image').removeClass('tv_img_selected');
        currentSelectedPage = uuid;
        var pageUrl = "djvu?uuid="+uuid+"&scaledWidth="+imgW;
        var mimeUrl = "djvu?uuid="+uuid+"&imageType=ask";
        var img = '<a class="lighbox" href="javascript:showFullImage(\''+uuid+'\')"><img id="imgBig" src="'+pageUrl+'" alt="" width="'+imgW+'px" border="0" onerror="showError();"  /></a>';
        checkArrows();
        if($('#imgBig').attr('src').indexOf(uuid)==-1){
            $('#mainContent').html(img);
        }

        $.get(mimeUrl, function(data){
            currentMime = data;
        });
        $('#img'+getMaxLevel()+'_'+uuid).toggleClass('tv_img_selected');
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
            //$('#djvuContainer').hide();
            $('#djvuContainer>object>param[name="src"]').attr('value', fullUrl);
            $('#djvuContainer>object>embed').attr('src', fullUrl);
            $('#djvuContainer').show();
        }else if(currentMime.indexOf('pdf') > 0){
            
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
        
        var to = $('#img'+getMaxLevel()+'_' + selection).offset().left - tvContainerLeft + $("#tv_container").attr("scrollLeft") - ($("#tv_container").width()/2) ;
        
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
    function slideTo(pos, selection){
        $("#tv_slider").slider("value", pos);
        
        //testujeme jestli obrazek je cely videt
        
        //var r = $('#img_' + selection).offset().left + $('#img_' + selection).width();
        //if(r>tvContainerRight){
        //    var pos2 = r - tvContainerLeft;
        //    slideTo(pos2, selection);
        //}
    }
    function getImgContainerWidth() {
        return "900px";	
    }
        
    var sliderCreated = false;
    
    var canScroll = true;
    function tv_SliderChange(e, ui){
        
        //if(maxScroll==0){
            maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        //    maxScroll = $("#tv_container").attr("scrollWidth") - $("#tv_container").width();
        //}
        if(canScroll){
            $("#tv_container").attr({
                scrollLeft: ui.value * (maxScroll / 100)
            });
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
<div id="tv" style="display:none;">
<div id="tv_container">
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
<table cellpadding="2" cellspacing="0" width="100%">
    <tr><td><div id="tv_slider" style="width: 100%"></div> </td></tr>
</table>
</div>
