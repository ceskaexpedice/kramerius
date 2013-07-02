<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<style type="text/css">
    #thumbs{
        overflow:hidden;
    }
    #tv{
        width: auto;
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
        width:80px;
        background:white  url(img/loading.gif) no-repeat 50% 50%;

    }
    #tv_container tr td div.t{
        height:134px;
        /*width:128px;*/
        border:solid white 1px;
        overflow:hidden;
        background:#F1F1F1;
        margin: 0px 4px;
        -moz-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
        -webkit-box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
        box-shadow:0 0 6px rgba(0, 0, 0, 0.5);
        position:relative;

    }
    #tv_container tr td div.sel{
        border:solid 2px #e75c01;
        padding:0;
    }
    #tv_container tr td div.t>img{
        height:128px;
        margin:3px;
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
        line-height: 17px;
        vertical-align: middle;
    }
    #tv_container_row div.inactive>img{
        width:80px;
    }

</style>
<div id="tv" class="viewer ui-widget ui-widget-content">
    <div id="tv_path"></div>
<div id="tv_container" align="center" >
    <table cellpadding="2" cellspacing="0" id="tv_container_table">
        <tr id="tv_container_row"></tr>
    </table>
</div>
</div>
<script type="text/javascript">

    var tv_containerRightBorder;
    var tv_containerLeftBorder;
    $(document).ready(function(){
        tv_containerRightBorder = $('#tv_container').offset().left + $('#tv_container').width() ;
        tv_containerLeftBorder = $('#tv_container').offset().left;
        $('#tv.viewer').bind('activeUuidsChanged', function(event, id){
            updateThumbs(id);
        });
        $('#tv.viewer').bind('viewReady', function(event, viewerOptions){
            selectThumb(viewerOptions.fullid);
        });
        $('#tv_container_row>td>div').live('click', function(){
            var id = $(this).attr('id').substring(3);
            initView = false;
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
            $('#tv_container_row').append('<td><div id="tv_'+k4Settings.activeUuids[i]+'" class="t inactive"><img src="img/empty.gif" />'+
                '<div id="dost_'+k4Settings.activeUuids[i]+'" style="position:absolute;left:2px;top:2px;"><img src="img/empty.gif" /></div></div>'+
                '</td>');
        }
        selectThumb(id);
        $('#tv_container_table').show();
        checkThumbsVisibility();
    }
    
    function checkThumbsVisibility(){
        $('#tv_container_row div.inactive').each(function(){
            checkIsThumbVisible($(this).attr("id").substring(3));
        });
    }
    
    function checkIsThumbVisible(uuid){
        var imgLeft = $(jq('tv_'+uuid)).offset().left;
        var imgRight = $(jq('tv_'+uuid)).offset().left + $(jq('tv_'+uuid)).width();
        var reserve = $('#tv_container').width();
        if(imgLeft<tv_containerRightBorder+reserve && imgRight>tv_containerLeftBorder-reserve){
            $(jq('tv_'+uuid)+'>img').attr('src', 'img?uuid='+uuid.split('_')[1]+'&stream=IMG_THUMB&action=GETRAW');
            $(jq('tv_'+uuid)).removeClass('inactive');
        }
        var p = isPrivate(uuid);
        if(p && !policyPublic){
            $(jq('dost_'+uuid)+'>img').attr('src', 'img/lock.png');
        }else if(!p && policyPublic){
            $(jq('dost_'+uuid)+'>img').attr('src', 'img/public.png');
        } 
    }
    
    function selectThumb(id){
        $('#tv_container_row>td>div').removeClass('sel');
        $(jq('tv_'+id)).addClass('sel');
        focusThumb(id);
    }
    
    function focusThumb(id){
        var l = $(jq('tv_'+id)).offset().left - $('#tv_container').offset().left + $('#tv_container').scrollLeft() - $('#tv_container').width()/2 ;

        $('#tv_container').scrollLeft(l);
    }
    
    function setThumbsPath(){
        //var p = '<span class="ui-icon ui-icon-triangle-1-e folder">folder</span>' + $(jq(k4Settings.selectedPath[0])+">a").html();
        var p = '';
        for(var i=0; i<k4Settings.selectedPathTexts.length; i++){
            var maxText = k4Settings.selectedPathTexts[i].toString();
            maxText = maxText.replaceAll("&nbsp;", " ").replace(/\n/g, "").trim().replace(/\s{2,}/g,' ');
            //alert(maxText.length + ": " + maxText);
            if (maxText.length > 40){
                maxText = maxText.substring(0,40) + "...";
            } 
            p += '<span class="ui-icon ui-icon-triangle-1-e">folder</span><span><a href="javascript:selectBranch(\'' + k4Settings.selectedPath[i] + '\');">' + maxText + '</a></span>' ;
        }
        $('#tv_path').html(p);
    }

</script>
