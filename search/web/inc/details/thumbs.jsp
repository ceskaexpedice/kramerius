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
    #tv_container div.t{
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
        display:inline-block;

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
    
    
    #tv_container_row{
        white-space: nowrap;
    }
    #tv_container_row>div.t>img{
        height:128px;
        margin:3px;
    }
    #tv_container_row div.inactive>img{
        width:80px;
    }
    #tv_container_row>div.inactive{
        width:80px;
    }
    
    #tv_container.loading{
        background: url('img/loading.gif') center center no-repeat;
        cursor: progress;
    }
    
    #tv_container_row>div.sel{
        border:solid 2px #e75c01;
        padding:0;
    }
    div.dost{
        position:absolute;left:2px;top:2px;
    }
    
    #tv_container_row input{
        position:absolute;right:1px;top:1px;
    }

</style>
<div id="tv" class="viewer ui-widget ui-widget-content">
    <div id="tv_path"></div>
<div id="tv_container" >
    <div id="tv_container_row"></div>
</div>
</div>
<script type="text/javascript">

    var tv_containerRightBorder;
    var tv_containerLeftBorder;
    $(document).ready(function(){
        tv_containerRightBorder = $('#tv_container').offset().left + $('#tv_container').width() ;
        tv_containerLeftBorder = $('#tv_container').offset().left;
        $('#tv.viewer').bind('activeUuidsChanged', function(event, id){
            $('#tv_container_row>div').remove();
            $('#tv_container').addClass('loading');
            setTimeout(function(){updateThumbs(id)}, 50);
        });
        $('#tv.viewer').bind('viewReady', function(event, viewerOptions){
            var id = viewerOptions.fullid;
            var index = -1;
            for(var i=0; i<k4Settings.activeUuids.length; i++){
                if(k4Settings.activeUuids[i] === id){
                    index = i;
                }
            }
            if(index > -1){
                selectThumb(id, $('#tv_container_row>div').eq(index));
            }
            
        });
        $('#tv.viewer').bind('selectedDocsChanged', function(event, id, checked){
            var index = -1;
            for(var i=0; i<k4Settings.activeUuids.length; i++){
                if(k4Settings.activeUuids[i] === id){
                    index = i;
                }
            }
            if(index > -1){
                $('#tv_container_row>div').eq(index).find('input').attr("checked", checked);
            }
            
        });
        $('#tv_container_row>div.t>img').live('click', function(){
            //var id = $(this).attr('id').substring(3);
            var id = k4Settings.activeUuids[$(this).parent().index()];
            initView = false;
            if(window.location.hash != id){
                window.location.hash = id;
            }
        });
        $('#tv_container_row>div>input').live('click', function(){
            //var id = $(this).prev().attr('id').substring(3);
            var id = k4Settings.activeUuids[$(this).parent().index()];
            $(jq(id)).find("input").attr("checked", $(this).is(":checked"));
            if($('#rightMenuBox').tabs( "option", "selected" ) ===2){
                renderSelection();
            }
        });
        $('#tv_container').bind('scroll', function(event){
            checkThumbsVisibility();
        });
        $('#tv.viewer').bind('selectedPathChanged', function(event, level){
            setThumbsPath();
        });
    });
    
    function updateThumbs(id){
        $('#tv_container_row>div').remove();
        //$('#tv_container_row').css('width', k4Settings.activeUuids.length * 90);
        var index = 0;
        for(var i=0; i<k4Settings.activeUuids.length; i++){
            var title = $(jq(k4Settings.activeUuids[i])).find("label").text().replaceAll("\n", " ").replace(/\s+/g," ");
            $('#tv_container_row').append('<div class="t inactive" title="'+title+'"></div>');
            if(k4Settings.activeUuids[i] === id){
                index = i;
            }
        }
        selectThumb(id, $('#tv_container_row>div').eq(index));
        $('#tv_container_table').show();
        checkThumbsVisibility();
        $('#tv_container').removeClass('loading');
    }
    
    function checkThumbsVisibility(){
        var isRightBorder = false;
        $('#tv_container_row>div.inactive').each(function(){
            if(!$(this).has('img.th').length){
                isRightBorder = checkIsThumbVisible($(this));
            }
            if(isRightBorder) return false;
        });
    }
    
    
    function checkIsThumbVisible(elem){
        var imgLeft = $(elem).offset().left;
        if (imgLeft < 0) return false;
        var imgRight = $(elem).offset().left + $(elem).width();
        var reserve = $('#tv_container').width();
        var ext_uuid = k4Settings.activeUuids[$(elem).index()];
        var uuid = ext_uuid.split('_')[1];
        if(imgLeft<tv_containerRightBorder+reserve && imgRight>tv_containerLeftBorder-reserve){
            var img = $('<img>', {src:'img?uuid='+uuid+'&stream=IMG_THUMB&action=GETRAW', class: 'th'});
            img.load(function(){$(elem).removeClass('inactive')});
            $(elem).append(img);
            var dost = $('<div>', {class: 'dost'});
            var p = isPrivate(ext_uuid);
            if(p && !policyPublic){
                dost.append('<img src="img/lock.png" />');
            }else if(!p && policyPublic){
                dost.append('<img src="img/public.png" />');
            } 
            $(elem).append(dost);
            var input = $('<input type="checkbox" />');
            var checked = $(jq(ext_uuid)).find('input').is(":checked");
            input.attr("checked", checked);
            $(elem).append(input);
        }
        return (imgLeft > tv_containerRightBorder+reserve);
    }
    
    function selectThumb(id, elem){
        $('#tv_container_row>div').removeClass('sel');
        $(elem).addClass('sel');
        focusThumb(id, elem);
    }
    
    function focusThumb(id, elem){
        if($(elem).length === 0) return;
        var l = $(elem).offset().left - $('#tv_container').offset().left + $('#tv_container').scrollLeft() - $('#tv_container').width()/2 ;

        $('#tv_container').scrollLeft(l);
    }
    
    function setThumbsPath(){
        var p = '';
        for(var i=0; i<k4Settings.selectedPathTexts.length; i++){
            var maxText = k4Settings.selectedPathTexts[i].toString();
            maxText = maxText.replaceAll("&nbsp;", " ").replace(/\n/g, "").trim().replace(/\s{2,}/g,' ');
            if (maxText.length > 40){
                maxText = maxText.substring(0,40) + "...";
            } 
            p += '<span class="ui-icon ui-icon-triangle-1-e">folder</span><span><a href="javascript:selectBranch(\'' + k4Settings.selectedPath[i] + '\');">' + maxText + '</a></span>' ;
        }
        $('#tv_path').html(p);
    }

</script>
