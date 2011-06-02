<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<style type="text/css">
    #tv{
        width: 100%;
    }
    #tv_container{
        width: 99%;
        height:160px;
        overflow: auto;
        position:relative;
        margin:5px;
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
    #tv_container tr td div.sel{
        border:solid 2px #e75c01;
        padding:0;
    }
    #tv_container tr td div img{
        height:128px;
    }

    #tv_scroll_table{
        padding-left:18px;
        padding-right:18px;
    }
    #tv_path{
        margin: 2px;
        padding-left: 12px;
        display:block;
        width:100%;
        height:1em;
    }
    #tv_path span{
        float:left;
    }

</style>
<div id="tv" class="viewer">
    <div id="tv_path"></div>
<div id="tv_container">
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
<table cellpadding="2" cellspacing="0" width="100%" id="tv_scroll_table">
    <tr><td><div id="tv_slider" style="width: 100%"></div> </td></tr>
</table>
</div>
<script type="text/javascript">

    var tv_containerRightBorder;
    var tv_containerLeftBorder;
    $(document).ready(function(){
        tv_containerRightBorder = $('#tv_container').offset().left + $('#tv_container').width() ;
        tv_containerLeftBorder = $('#tv_container').offset().left;
        //$('#tv').css('width', $(window).width()-25);
        $('#tv.viewer').bind('activeUuidsChanged', function(event, id){
            updateThumbs(id);
        });
        $('#tv.viewer').bind('viewReady', function(event, viewerOptions){
            selectThumb(viewerOptions.fullid);
        });
        $('#tv_container_row>td>div').live('click', function(){
            var id = $(this).attr('id').substring(3);
            selectThumb(id);
            $(".viewer").trigger('viewChanged', [id]);
        });
        $('#tv_container').bind('scroll', function(event){
            checkThumbsVisibility();
        });
        $('#tv.viewer').bind('selectedPathChanged', function(event, level){
            setThumbsPath();
        });
    });

    function updateThumbs(id){
        $('#tv_container_row>td').remove();
        for(var i=0; i<k4Settings.activeUuids.length; i++){
            $('#tv_container_row').append('<td><div id="tv_'+k4Settings.activeUuids[i]+'"><img src="../empty.gif" /></div></td>');
        }
        selectThumb(id);
        $('#tv_container_table').show();
        checkThumbsVisibility();
    }
    
    function checkThumbsVisibility(){
        for(var i=0; i<k4Settings.activeUuids.length; i++){
            checkIsThumbVisible(k4Settings.activeUuids[i]);
        }
    }
    
    function checkIsThumbVisible(uuid){
        var imgLeft = $('#tv_'+uuid).offset().left;
        var imgRight = $('#tv_'+uuid).offset().left + $('#tv_'+uuid).width();
        var reserve = $('#tv_container').width();
        if(imgLeft<tv_containerRightBorder+reserve && imgRight>tv_containerLeftBorder-reserve){
            $('#tv_'+uuid+'>img').attr('src', '../img?uuid='+uuid.split('_')[1]+'&stream=IMG_THUMB&action=GETRAW');
        }
    }
    
    function selectThumb(id){
        $('#tv_container_row>td>div').removeClass('sel');
        $('#tv_'+id).addClass('sel');
        focusThumb(id);
    }
    
    function focusThumb(id){
        var l = $('#tv_'+id).offset().left - $('#tv_container').offset().left + $('#tv_container').scrollLeft() - $('#tv_container').width()/2 ;

        $('#tv_container').scrollLeft(l);
    }
    
    function setThumbsPath(){
        //var p = '<span class="ui-icon ui-icon-triangle-1-e folder">folder</span>' + $("#"+k4Settings.selectedPath[0]+">a").html();
        var p = '';
        for(var i=0; i<k4Settings.selectedPath.length; i++){
            p += '<span class="ui-icon ui-icon-triangle-1-e">folder</span><span>' + $("#"+k4Settings.selectedPath[i]+">a").html() + '</span>' ;
        }
        $('#tv_path').html(p);
    }

</script>
