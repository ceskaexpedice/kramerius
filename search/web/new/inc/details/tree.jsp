<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<style type="text/css">
    #item_tree{
        padding-left: 2px;
        margin: 2px;
    }
    #item_tree li a{
        overflow:hidden;
    }
    #item_tree li a.sel{
        color:#e66c00;
    }
    #showHideRightMenu{
        width:20px;
        float:left;
        margin:1px;
        right:0px;
        top:0px;
        padding:1px;
    }
    #rightMenuBox{
        width:330px;
        /*margin-left:22px;*/
    }
    #rightMenuBox h3{
        margin:0px;
    }
    #rightMenuBox>div{
        overflow: auto;
        height:500px;
    }
    #rightMenuBox .searchQuery{
        width:200px;
        height:1.1em;
        margin-right:5px;
    }
    #searchInsideScope{
        margin:5px;
        padding-left: 12px;
    }
    #searchInsideScope li{
        list-style-type: none;
        margin: 0;
        padding: 0;
        line-height: 16px;
    }
    #searchInsideScope li span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }

</style>
<c:url var="url" value="${kconfig.applicationURL}/new/inc/details/treeNodeInfo.jsp" >
    <c:param name="pid" value="${root_pid}" />
    <c:param name="path" value="${root_model}" />
</c:url>
<c:import url="${url}" var="infoa" charEncoding="UTF-8"  />
<%--
<div id="showHideRightMenu" class="shadow"><a href="javascript:toggleRightMenu();" title="<fmt:message bundle="${lctx}">item.showhide</fmt:message>"><span class="ui-state-default ui-icon ui-icon-circle-triangle-e"></span></a></div>
--%>
<div id="rightMenuBox" class="shadow_">
<%--    
    <h3>
        <input type="text" value="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>" id="insideQuery" size="25" class="searchQuery">
        <a href="javascript:searchInside();"><img border="0" align="top" alt="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>" src="img/lupa_orange.png" /></a>
        <a href="javascript:showContextMenu();" title="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>"><img border="0" align="top" alt="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" src="img/gear.png" /></a>
        <a href="javascript:showStructure();"><img border="0" align="top" alt="<fmt:message bundle="${lctx}">item.structure</fmt:message>" src="img/tree.png" /></a>
        
    </h3>
    <div style="overflow: auto; height:500px;" >
        <ul id="item_tree" class="viewer">
            <li id="${root_model}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                <a href="#"><fmt:message bundle="${lctx}">fedora.model.${root_model}</fmt:message></a>
                <ul><li id="${root_model}_${root_pid}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                        <a href="#">${infoa}</a></li></ul>
            </li>
        </ul>
        <div id="contextMenu"><%@include file="contextMenu.jsp" %></div>
        <div id="searchInside"><h3><a href="javascript:closeSearchInside();"><span class="ui-icon ui-icon-closethick " >close</span></a></h3><div id="searchInsideResults"></div></div>
    </div>
--%>
    <ul>   
        <li><a href="#structure" title="<fmt:message bundle="${lctx}">item.structure</fmt:message>"><img border="0" height="17" alt="<fmt:message bundle="${lctx}">item.structure</fmt:message>" src="img/tree.png" /></a></li>
        <li>
        <a href="#searchInside" title="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>"><img border="0" alt="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>" src="img/lupa_orange.png" /></a>
        </li>
        <li>
            <a href="#contextMenu" title="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>"><img height="17" border="0" alt="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" src="img/gear.png" /></a>
        </li>    
    </ul>
    <div id="structure"  >
        <ul id="item_tree" class="viewer">
            <li id="${root_model}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                <a href="#" class="model"><fmt:message bundle="${lctx}">fedora.model.${root_model}</fmt:message></a>
                <ul><li id="${root_model}_${root_pid}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                        <input type="checkbox" /><a href="#" class="label">${infoa}</a></li></ul>
            </li>
        </ul>
    </div>
    <div id="contextMenu"><%@include file="contextMenu.jsp" %></div>
    <div id="searchInside">
        <fmt:message bundle="${lctx}">administrator.menu.selected.scope</fmt:message>:
        <ul id="searchInsideScope"></ul>
        <div>
            <input type="text"  id="insideQuery" size="25" class="searchQuery" onclick="checkInsideInput();" <c:choose>
                   <c:when test="${empty param.q}">value="<fmt:message bundle="${lctx}" key="administrator.menu.searchinside" />"</c:when>
                   <c:otherwise>value="${param.q}"</c:otherwise>
            </c:choose> />
            <a href="javascript:searchInside();"><img border="0" align="top" alt="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>" src="img/lupa_orange.png" /></a>
        </div>
        <div id="searchInsideResults"></div>
    </div>
</div>
<script type="text/javascript">
    var pid_path_str = '${pid_path}';
    var model_path_str = '${path}';
    var pid_path = pid_path_str.split('/');
    var model_path = model_path_str.split('/');
        $(document).ready(function(){
            $('#item_tree').css('width', $('#itemTree').width()-20);
            $('#rightMenuBox>h3').addClass('ui-state-default ui-corner-top ui-tabs-selected ui-state-active ');
            $("#item_tree li>span.folder").live('click', function(event){
                var id = $(this).parent().attr('id');
                nodeOpen(id);
                event.stopPropagation();
            });
            $('#rightMenuBox').tabs({
                show: function(event, ui){
                    var tab = ui.tab.toString().split('#')[1];
                    var t = "";
                    if (tab=="contextMenu"){
                        $('#item_tree input:checked').each(function(){
                            var id = $(this).parent().attr("id");
                            t += '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>'+$("#"+id+">a").html()+'</li>';
                        });
                        $('#context_items_selection').html(t);
                        t = '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>'+$("#"+k4Settings.activeUuid+">a").html()+'</li>';
                        $('#context_items_active').html(t);
                    }else{
                        if($('#item_tree input:checked').length>0){
                            $('#item_tree input:checked').each(function(){
                                var id = $(this).parent().attr("id");
                                t += '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>'+$("#"+id+">a").html()+'</li>';
                            });
                        }else{
                            var id = $('#item_tree>li>ul>li:first').attr("id");
                            t = '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>'+$("#"+id+">a").html()+'</li>';
                        }
                        $('#searchInsideScope').html(t);
                    }
                }
            });

            $("#item_tree li>a").live('click', function(event){
                var id = $(this).parent().attr('id');
                nodeClick(id);
                event.preventDefault();
                event.stopPropagation();
            });

            $('#item_tree.viewer').bind('viewChanged', function(event, id){
                selectNodeView(id);
            });
            loadInitNodes();
        });
        var cur = 1;
        var loadingInitNodes = true;
        function loadInitNodes(){
            var id;
            var path = "";
            if(pid_path.length>cur-1){
                for(var i = 0; i<cur; i++){
                    if(path!="") path = path + "-";
                    path = path + model_path[i];
                }
                id = path + "_" + pid_path[cur-1];
                cur++;
                if($("#"+id+">ul").length>0){
                    loadInitNodes();
                }else{
                    loadTreeNode(id);
                }
                //nodeClick(id);
            }else{
                for(var i = 0; i<pid_path.length; i++){
                    if(path!="") path = path + "-";
                    path = path + model_path[i];
                }
                id = path + "_" + pid_path[pid_path.length-1];
                //alert(id);
                while(!$("#"+id).hasClass('viewable')){
                    if($("#"+id+">ul").length>0){
                        id = $("#"+id+">ul>li:first").attr("id");
                    }else{
                        loadTreeNode(id);
                        return;
                    }
                }
                loadingInitNodes= false;
                //alert(id);
                showNode(id);
                setActiveUuids(id);
                $(".viewer").trigger('viewChanged', [id]);
                //setTimeout("toggleRightMenu('slow')", 3000);
            }
        }

        function highLigthNode(id){
            $("#"+id+">a").addClass('sel');
            $("#"+id).addClass('sel');
            if($("#"+id).parent().parent().is('li')){
                highLigthNode($($("#"+id).parent().parent()).attr('id'));
            }
        }

        function showNode(id){
            $("#"+id+">ul").show();
            $("#"+id+">span.folder").addClass('ui-icon-triangle-1-s');
            $("#"+id+">a").addClass('sel');
            $("#"+id).addClass('sel');
            if($("#"+id).parent().parent().is('li')){
                showNode($($("#"+id).parent().parent()).attr('id'));
            }
        }

        function nodeClick(id){
            if($("#"+id).hasClass('viewable')){
                selectNodeView(id);
                nodeOpen(id);
                $(".viewer").trigger('viewChanged', [id]);
            }else{
                nodeOpen(id);
            }
        }

        function selectNodeView(id){
            var fire = false;
            if(!$("#"+id).parent().parent().hasClass('sel')){
                fire = true;
            }
            $("#item_tree li>a").removeClass('sel');
            $("#item_tree li").removeClass('sel');
            highLigthNode(id);
            if(fire){
                setActiveUuids(id);
            }
            setSelectedPath(id);
        }

        function nodeOpen(id){
            if($("#"+id+">ul").length>0){
                $("#"+id+">ul").toggle();
            }else{
                loadTreeNode(id);
            }
            $("#"+id+">span.folder").toggleClass('ui-icon-triangle-1-s');
        }

        function loadTreeNode(id){
            var pid = id.split('_')[1];
            var path = id.split('_')[0];
            var url = 'inc/details/treeNode.jsp?pid=' + pid + '&path=' + path;
            $.get(url, function(data){
                var d = trim10(data);
                if(d.length>0){
                    $("#"+id).append(d);
                }else{
                    $("#"+id+">span.folder").removeClass();
                }
                if(loadingInitNodes) loadInitNodes();
            });
        }

        function setActiveUuids(id){
            k4Settings.activeUuids = [];
            var i = 0;
            $("#"+id).parent().children('li').each(function(){
                k4Settings.activeUuids[i] = $(this).attr('id');
                i++;
            });
            
            $(".viewer").trigger('activeUuidsChanged', [id]);
        }

        function getPath(id){
            var curid = id;
            var selectedPathTemp = "";
            while($("#"+curid).parent().parent().is('li')){
                if(!$("#"+curid).hasClass('model')){
                    if(selectedPathTemp!="") selectedPathTemp = "/" + selectedPathTemp;
                    selectedPathTemp = curid.split('_')[1] + selectedPathTemp;
                }
                curid = $($("#"+curid).parent().parent()).attr('id');
            }
            return selectedPathTemp;
        }

        function setSelectedPath(id){
            var curid = id;
            var selectedPathTemp = [];
            var i = 0;
            while($("#"+curid).parent().parent().is('li')){
                if(!$("#"+curid).hasClass('model')){
                    selectedPathTemp.push(curid);
                }
                curid = $($("#"+curid).parent().parent()).attr('id');
                i++;
            }
            selectedPathTemp.reverse();
            var level = selectedPathTemp.length-1;
            for(var j=0;j<selectedPathTemp.length; j++){
                if(k4Settings.selectedPath[j]!=selectedPathTemp[j]){
                    level = j;
                    break;
                }
            }
            k4Settings.selectedPath = [];
            for(var j=0;j<selectedPathTemp.length; j++){
                k4Settings.selectedPath[j]=selectedPathTemp[j];
            }
            $(".viewer").trigger('selectedPathChanged', [level]);
        }

        function toggleRightMenu(speed){
            if(speed){
                $('#rightMenuBox').toggle(2000);
            }else{
                $('#rightMenuBox').toggle();
            }
            $('#showHideRightMenu>a>span').toggleClass('ui-icon-circle-triangle-w');
        }

        function showContextMenu(){
            $('#item_tree input:checked').each(function(){
                var id = $(this).parent().attr("id");
                $('#context_items').append('<div>'+id+'</div>');
            });
            
            $('#contextMenu').show();
            $('#item_tree').hide();
            $('#searchInside').hide();
        }

        function showStructure(){
            $('#searchInside').hide();
            $('#contextMenu').hide();
            $('#item_tree').show();
        }

        function closeSearchInside(){
            $('#searchInside').hide();
            $('#contextMenu').hide();
            $('#item_tree').show();
        }

        function searchInside(start){
            var offset = start ? start : 0;
            //$('#contextMenu').hide();
            //$('#item_tree').hide();
            //$('#searchInside').show();
            $('#searchInsideResults').html('<img alt="loading" src="img/loading.gif" />');
            var q = $('#insideQuery').val();
            var fq = "";
            if($('#item_tree input:checked').length>0){
                $('#item_tree input:checked').each(function(){
                    var id = $(this).parent().attr("id");//.split('_')[1];
                    if(fq!=""){
                        fq += " OR ";
                    }
                    fq += "pid_path:" + getPath(id) + "*";
                });
                fq = "&fq=" + fq;
            }else{
                fq = "&fq=pid_path:" + $('#item_tree>li>ul>li:first').attr("id").split('_')[1]+"*";
            }
            //var url = "searchXSL.jsp?q="+q+"&offset="+offset+"&xsl=insearch.xsl&collapsed=false&facet=false&fq=pid_path:"+pid+"*";
            var url = "inc/details/searchInside.jsp?q="+q+"&offset="+offset+"&xsl=insearch.xsl&collapsed=false&facet=false" + fq;
            $.get(url, function(data){
                $('#searchInsideResults').html(data);
            });
        }

        var inputInitialized = false;
        function checkInsideInput(){
            var iniVal = '<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>';
            var q = $('#insideQuery').val();
            if(!inputInitialized && iniVal == q){
                inputInitialized = true;
                $('#insideQuery').val('');
            }
        }

</script>
            <div id="test" style="position:fixed;top:0px;left:0px;background:white;" ></div>