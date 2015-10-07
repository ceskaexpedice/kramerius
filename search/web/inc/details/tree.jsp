<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*"%>
<view:kconfig var="expandTree" key="search.expand.tree" defaultValue="true" />
<style type="text/css">
    #item_tree{
        padding-left: 2px;
        margin: 2px;
    }
    #item_tree ul{
        margin: 2px;
        padding-left: 8px;
    }
    #item_tree li a{
        overflow:hidden;
    }
    #item_tree li a.sel{
        /*color:#e66c00;*/
        border:none;
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
       /*  width:100%;
       margin-left:22px;*/
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
    #searchInsideScope li>span{
        width: 16px;
        height: 16px;
        overflow:hidden;
        text-indent: -99999px;
        display:block;
        float:left;
    }
    #structure{
        margin: 2px;
        padding-left: 8px;
    }

</style>

<c:set var="class_viewable"><c:if test="${viewable=='true' && root_pid==param.pid}">viewable</c:if></c:set>
<c:url var="url" value="${kconfig.applicationURL}/inc/details/treeNodeInfo.jsp" >
    <c:param name="pid" value="${root_pid}" />
    <c:param name="model_path" value="${root_model}" />
</c:url>
<c:import url="${url}" var="infoa" charEncoding="UTF-8"  />
<%--
<div id="showHideRightMenu" class="shadow"><a href="javascript:toggleRightMenu();" title="<fmt:message bundle="${lctx}">item.showhide</fmt:message>"><span class="ui-state-default ui-icon ui-icon-circle-triangle-e"></span></a></div>
--%>
<div id="rightMenuBox" class="shadow_">
    <ul>   
        <li><a href="#structure" title="<fmt:message bundle="${lctx}" key="item.structure" />"><span class="ui-icon ui-icon-folder-open"><fmt:message bundle="${lctx}">item.structure</fmt:message></span></a></li>
        <li><a href="#searchInside" title="<fmt:message bundle="${lctx}" key="administrator.menu.searchinside"/>"><span class="ui-icon ui-icon-search"><fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message></span></a></li>
        <li><a href="#contextMenu" title="<fmt:message bundle="${lctx}" key="administrator.menu" />"><span  class="ui-icon ui-icon-gear" ><fmt:message bundle="${lctx}">administrator.menu</fmt:message></span></a></li>
        <view:kconfig var="showSuggest" key="search.details.showSuggest" />
        <c:if test="${!empty showSuggest && showSuggest=='true' }">
        <li><a href="#suggest" title="<fmt:message bundle="${lctx}" key="search.results.suggested.documents" />"><span  class="ui-icon ui-icon-lightbulb" ><fmt:message bundle="${lctx}" key="search.results.suggested.documents" /></span></a></li>
        </c:if>
    </ul>
    <div id="structure"  >
        <ul id="item_tree" class="viewer">
            <li id="${root_model}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                <a href="#" class="model"><fmt:message bundle="${lctx}">fedora.model.${root_model}</fmt:message></a>
                <ul><li id="${root_model}_${root_pid}" class="${class_viewable} ${dostupnost}"><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>
                        <div style="float:left;"><input type="checkbox"  /></div>
                <div style="float:left;"><a href="#" class="label">${infoa}</a></div></li></ul>
            </li>
        </ul>
    <div id="donator" class="viewer" style="position:relative; bottom:0px; width:100%; text-align:center;clear:both;"></div>  
    </div>
    <div id="contextMenu"><%@include file="contextMenu.jsp" %></div>
    <div id="searchInside">
        <fmt:message bundle="${lctx}">administrator.menu.selected.scope</fmt:message>:
        <ul id="searchInsideScope"></ul>
        <div>
            <input type="text"  id="insideQuery" size="25" class="searchQuery" onkeypress="checkEnter();" onclick="checkInsideInput();" value="${param.q}" />
            <a href="javascript:searchInside();"><img border="0" align="top" alt="<fmt:message bundle="${lctx}">administrator.menu.searchinside</fmt:message>" src="img/lupa_orange.png" /></a>
        </div>
        <div id="searchInsideResults"></div>
    </div> 
    <c:if test="${!empty showSuggest && showSuggest=='true' }">
    <div id="suggest" style="padding:3px;" class="viewer">
        <div><h3><view:msg>search.results.suggested.documents</view:msg></h3></div>
        <div class="content"></div>
    </div>
    </c:if>
</div> 
<script type="text/javascript">
    var pid_path_str = '${pid_path}';
    var model_path_str = '${model_path}';
    var pid_path = pid_path_str.split('/');
    var model_path = model_path_str.split('/');
    var loadingInitNodes;
        $(document).ready(function(){
            loadingInitNodes = true;
            $('#item_tree').css('width', $('#itemTree').width()-20);
            $('#rightMenuBox>h3').addClass('ui-state-default ui-corner-top ui-tabs-selected ui-state-active ');
            $("#item_tree li>span.folder").live('click', function(event){
                var id = $(this).parent().attr('id');
                nodeOpen(id);
                event.stopPropagation();
            });
            
            $('#item_tree li>div>input').live('click', function(){
                var id = $(this).parent().parent().attr('id');
                $('#tv.viewer').trigger('selectedDocsChanged', [id, $(this).is(":checked")]);
            });
            $('#rightMenuBox').tabs({
                show: function(event, ui){
                    var tab = ui.tab.toString().split('#')[1];
                    var t = "";
                    if (tab=="contextMenu"){
                        renderSelection();
                    }else{
                        if($('#item_tree input:checked').length>0){
                            $('#item_tree input:checked').each(function(){
                                var id = $(this).parent().parent().attr("id");
                                t += '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span><label>'+$(jq(id)+">div>a>label").html()+'</label></li>';
                            });
                        }else{
                            var id = $('#item_tree>li>ul>li:first').attr("id");
                            t = '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span><label>'+$(jq(id)+">div>a>label").html()+'</label></li>';
                        }
                        $('#searchInsideScope').html(t);
                    }
                    onShowContextMenu();
                }
            });

            $("#item_tree li>div>a").live('click', function(event){
                var id = $(this).parent().parent().attr('id');
                nodeClick(id);
                event.preventDefault();
                event.stopPropagation();
            });

            $('#item_tree.viewer').bind('viewChanged', function(event, id){
                selectNodeView(id);
            });

            $('#donator.viewer').bind('viewReady', function(event, viewerOptions){
                checkDonator(viewerOptions);
            });
<c:if test="${!empty showSuggest && showSuggest=='true' }">
            $('#suggest.viewer').bind('viewReady', function(event, viewerOptions){
                getSuggested(viewerOptions);
            });
</c:if>            
            loadInitNodes();
            $(window).bind( 'hashchange', function(e) {
                checkHashChanged(e);
            });
        });
        var cur = 1;
        var initView = true;
        
        function renderSelection(){
            var t="";
            $('#item_tree input:checked').each(function(){
                var id = $(this).parent().parent().attr("id");
                t += '<li id="cm_' + id + '">';
                t += '<span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>';
                t += '<label>'+$(jq(id)+">div>a>label").html()+'</label></li>';
                //t += '<li><span class="ui-icon ui-icon-triangle-1-e folder " >folder</span>'+$(jq(id)+">a").html()+'</li>';
            });
            $('#context_items_selection').html(t);
        }

        function loadInitNodes(){
            var id;
            var path = "";
            if(pid_path.length>cur-1){
                for(var i = 0; i<cur; i++){
                    if(path!="") path = path + "-";
                    path = path + model_path[i];
                }
                
                id = path + "_" + pid_path[cur-1];
                
                if(pid_path[cur-1].indexOf("@")!=0){
                    id = path + "_" + pid_path[cur-1];
                }else{
                    id = path + "_" + pid_path[cur-2] + "/" + pid_path[cur-1];
                }
                cur++;
                if($(jq(id)+">ul>li").length>0){
                    loadInitNodes();
                }else{
                    loadTreeNode(id);
                }
            }else{
                for(var i = 0; i<pid_path.length; i++){
                    if(pid_path[i].indexOf("@")!=0){
                        if(path!="") path = path + "-";
                        path = path + model_path[i];
                    }
                }
                if(pid_path[pid_path.length-1].indexOf("@")!=0){
                    id = path + "_" + pid_path[pid_path.length-1];
                }else{
                    id = path + "_" + pid_path[pid_path.length-2];
                }
                while(!$(jq(id)).hasClass('viewable')){
                    if($(jq(id)+">ul>li").length>0){
                        id = $(jq(id)+">ul>li:first").attr("id");
                    }else if($(jq(id)+">ul").length>0){
                        break;
                    }else{
                        if(id){
                            if(id.split('_')[1].indexOf("@")!=0){
        <c:if test="${expandTree}">                                
                                loadTreeNode(id);
        </c:if>                        
                                return;
                            }
                        } 
                    }
                }
                loadingInitNodes= false;
                if(id){
                    showNode(id);
                    setActiveUuids(id);
                    initView = true;
                    setInitActive();
                    $(".viewer").trigger('viewChanged', [id]);
                }
                 
            }
            
        }

        function highLigthNode(id){
            $(jq(id)+">div>a").addClass('sel');
            $(jq(id)).addClass('sel');
            $(jq(id)+">div>a").addClass('ui-state-active');
            if($(jq(id)).parent().parent().is('li')){
                highLigthNode($($(jq(id)).parent().parent()).attr('id'));
            }
        }

        function showNode(id){
            $(jq(id)+">ul").show();
            $(jq(id)+">span.folder").addClass('ui-icon-triangle-1-s');
            $(jq(id)+">div>a").addClass('sel');
            $(jq(id)+">div>a").addClass('ui-state-active');
            $(jq(id)).addClass('sel');
            if($(jq(id)).parent().parent().is('li')){
                showNode($($(jq(id)).parent().parent()).attr('id'));
            }
        }

        function nodeClick(id){
            initView = false;
            if($(jq(id)).hasClass('viewable')){
                nodeOpen(id);
                if(window.location.hash != id){
                    window.location.hash = id;
                }
            }else{
                nodeOpen(id);
            }
        }
        
        function selectBranch(id){
            var node =  $(jq(id));
            if(node.hasClass('viewable')){
                selectNodeView(id);
                nodeOpen(id);
                if($(jq(id)+">ul").html().trim().length > 0){
                    $(jq(id)+">ul").show();
                    $(jq(id)+">span.folder").toggleClass('ui-icon-triangle-1-s');
                }
                

                $(".viewer").trigger('viewChanged', [id]);
            }else{
                nodeOpen(id);
                if($(jq(id)+">ul").html().trim().length > 0){
                    $(jq(id)+">ul").show();
                    $(jq(id)+">span.folder").toggleClass('ui-icon-triangle-1-s');
                    var id1 = $(node).find('>ul>li>ul>li:first');
                    if(id1.length>0){
                        selectBranch(id1.attr("id"));
                    }
                }
                
            }
        }

        function selectNodeView(id){
            
            $("#item_tree li>div>a").removeClass('sel');
            $("#item_tree li").removeClass('sel');
            $("#item_tree li>div>a").removeClass('ui-state-active');
            $("#item_tree li").removeClass('ui-state-active');
            highLigthNode(id);
            setActiveUuids(id);
                
            setSelectedPath(id);
            
        }
        
        function checkHashChanged(e){
            var id =  k4Settings.activePidPath;
            var newid =  window.location.hash.toString().substring(1);
            if(id != newid){
                if(newid.length==0){
                    loadInitNodes();
                }
                selectNodeView(id);
                //nodeClick(newid);
                $(".viewer").trigger('viewChanged', [newid]);
            }
        }

        function nodeOpen(id){
            if($(jq(id)+">ul").length>0){
                if($(jq(id)+">ul").html().trim().length>0){
                    $(jq(id)+">ul").toggle();
                }
                
            }else{
                loadTreeNode(id);
            }
            $(jq(id)+">span.folder").toggleClass('ui-icon-triangle-1-s');
        }

        var autoLoaded = [];
        function loadTreeNode(id){
            if(autoLoaded[id]){
                renderNode(id, autoLoaded[id]);
                return;
            }
            var pid = id.split('_')[1];
            
            var path = id.split('_')[0];
            var url = 'inc/details/treeNode.jsp?pid=' + pid + '&model_path=' + path;
            $.get(url, function(data){
                var d = trim10(data);
                autoLoaded[id] = d;
                renderNode(id, d);
            });
        }
        
        function renderNode(id, d){
            if(d.length>0){
                $(jq(id)+">ul").remove();
                $(jq(id)).append(d);
                if($(jq(id)+">ul").html()==null || $(jq(id)+">ul").html().trim().length==0){
                    $(jq(id)+">ul").hide();
                }
            }else{
                $(jq(id)+">span.folder").removeClass();
            }
            if(loadingInitNodes){
                loadInitNodes();
            }
        }

        function setActiveUuids(id){
            var oldPidPath = k4Settings.activePidPath;
            
            k4Settings.activePidPath = id;
            
            if(oldPidPath!== null && $(jq(id)).parent()[0] === $(jq(oldPidPath)).parent()[0]){
                //jenom se zmenil pid, ale jsme na stejne vetvi
                return;
            }
            
        
            k4Settings.activeUuids = [];
            
            var i = 0;
            $(jq(id)).parent().children('li').each(function(){
                k4Settings.activeUuids[i] = $(this).attr('id');
                i++;
            });
            
            $(".viewer").trigger('activeUuidsChanged', [id]);
        }

        function getPidPath(id){
            var curid = id;
            var selectedPathTemp = "";
            while($(jq(curid)).parent().parent().is('li')){
                if(!$(jq(curid)).hasClass('model')){
                    if(selectedPathTemp!="") selectedPathTemp = "/" + selectedPathTemp;
                    selectedPathTemp = curid.split('_')[1] + selectedPathTemp;
                }
                curid = $($(jq(curid)).parent().parent()).attr('id');
            }
            return selectedPathTemp;
        }

        function setSelectedPath(id){
            var curid = id;
            var selectedPathTemp = [];
            var i = 0;
            while($(jq(curid)).parent().parent().is('li')){
                if(!$(jq(curid)).hasClass('model')){
                    selectedPathTemp.push(curid);
                }
                curid = $($(jq(curid)).parent().parent()).attr('id');
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
            k4Settings.selectedPathTexts = [];
            for(var j=0;j<selectedPathTemp.length; j++){
                k4Settings.selectedPath[j]=selectedPathTemp[j];
                var html = $(jq(selectedPathTemp[j])+">div>a>label").html();
                if(html==null) html = '';
                k4Settings.selectedPathTexts[j]= html;
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
                var id = $(this).parent().parent().attr("id");
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
                    var id = $(this).parent().parent().attr("id");//.split('_')[1];
                    if(fq!=""){
                        fq += " OR ";
                    }
                    fq += "pid_path:" + getPidPath(id).replace(/:/g, "\\:") + "*";
                });
                fq = "&fq=" + fq;
            }else{
                var fqval = $('#item_tree>li>ul>li:first').attr("id").split('_')[1];
                fq = "&fq=pid_path:" + fqval.replace(/:/g, "\\:") + "*";
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
        
        function checkEnter(evn){
            if (window.event && window.event.keyCode == 13) {
                searchInside(0);
              } else if (evn && evn.keyCode == 13) {
                searchInside(0);
              }
        }
        
     function getTreeSelection(){
        var uuids = [];
        $('#item_tree input:checked').each(function(){
            var id = $(this).parent().parent().attr("id");
            uuids.push(id);
        });
        return uuids;
    }
    
    function checkDonator(id){
            $.get('inc/details/donator.jsp?uuid='+k4Settings.selectedPath[0].split("_")[1], function(data){
                $('#donator').html(data);
            });
    }

<c:if test="${!empty showSuggest && showSuggest=='true' }">   
    function getSuggested(viewerOptions){
            $.get('inc/details/suggest.jsp?pid='+viewerOptions.pid+
                "&pid_path="+getPidPath(k4Settings.activePidPath), function(data){
                $('#suggest>div.content').html(data);
            });
    }
</c:if> 
</script>
            <div id="test" style="position:fixed;top:0px;left:0px;background:white;" ></div>