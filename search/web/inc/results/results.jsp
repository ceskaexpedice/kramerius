<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<style type="text/css">
    #filters>ul>li>a{
        line-height: 17px;
    }
    
    #docs_content{
        padding:4px;
        
    }
    #docs_content>div.header{
        height:17px;
        border-bottom:1px solid #E66C00; 
        margin-bottom:2px;
    }
    #docs_content>div.content{
        overflow:auto;
    }
    
    .extInfo{
        /*font-style: italic;*/
    }
    
    .collapse_label{
        position:absolute;
        bottom:5px;
        right:5px;
        width:100%;
    }
    
    #split {
        height: 700px;
        width: 1000px;
    }

</style>
<div id="filters" class="ui-layout-west">
    <ul>
        <li><a href="#facets"><fmt:message bundle="${lctx}">results.filters</fmt:message></a></li>
        <li id="dali"><a href="#dadiv"><fmt:message bundle="${lctx}" key="Časová osa" /></a></li>
        <scrd:loggedusers>
        <li>
            <a href="#contextMenu" title="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>"><img height="17" border="0" alt="<fmt:message bundle="${lctx}">administrator.menu</fmt:message>" src="img/gear.png" /></a>
        </li>   
        </scrd:loggedusers>
    </ul>
    <div id="facets">
    <%@ include file="../usedFilters.jsp" %>
    <%@ include file="../facets.jsp" %>
    </div>
    <div id="dadiv" style="padding:3px;"><%@ include file="../da.jsp" %></div>
    <%--
    <div id="dateAxis" class="shadow box" style="float:right;right:0;z-index:2;background:white;position:absolute;">
    <div id="showHideDA" ><a href="javascript:toggleDA();" title="show/hide date axis"><span class="ui-state-default ui-icon ui-icon-image ui-icon-circle-triangle-e"></span></a></div>
        <div id="daBox">
            <%@include file="../dateAxisV.jsp" %>
        </div>
    </div>
    --%>
    <scrd:loggedusers>
    <div id="contextMenu"><%@include file="../details/contextMenu.jsp" %></div>
    </scrd:loggedusers>
</div>
<div id="docs" class="ui-layout-center">
    <ul><li><a href="#docs_content"><fmt:message bundle="${lctx}">results.results</fmt:message></a></li></ul>
    <div id="docs_content">
    <%@ include file="head.jsp" %>
    <div class="content"><%@ include file="docs.jsp" %></div>
    </div>
</div>
<script type="text/javascript">


$(document).ready(function(){
    
    var w = $(window).height() -
            $("#header").height() - 
            $("#footer").outerHeight(true);
    $("#split").css("height", w);
    w = w - $("#docs>ul").outerHeight(true) - 6 - 15 - 8;
    $("#docs_content>div.content").css("height", w);
        
    sp = $("#split").layout({
        west:{
            size:220,
            spacing_closed:	5,
            spacing_open:	5,
            togglerLength_closed:	'100%',
            togglerLength_open:	'100%',
            togglerAlign_open:	"top",
            togglerAlign_closed:	"top",
            togglerTip_closed: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
            togglerTip_open: '<fmt:message bundle="${lctx}">item.showhide</fmt:message>',
            onopen_end: function(){
                setColumnsWidth();
            },
            onclose_end: function(){
                setColumnsWidth();
            }
        }
        
    });
    getExtInfo();
    $('.loading_docs').hide();
    
    if($('#dadiv').length==0){
        $("#dali").remove();
    }
    
    $("#filters").tabs({
        show: function(event, ui){
            var tab = ui.tab.toString().split('#')[1];
            if (tab=='dadiv'){
                 positionCurtains();
                 setBarsPositions();

            }
        }
    });
    $("#docs").tabs();
    
    //$(document).bind('scroll', function(event){
    $('#docs_content>div.content').bind('scroll', function(event){
        if($('#docs_content .more_docs').length>0){
            var id = $('#docs_content .more_docs').attr('id');
            //if(isScrolledIntoWindow($('#'+id))){
            if(isScrolledIntoView($('#'+id), $('#docs_content>div.content'))){
                getMoreDocs(id);
                //alert(id);
            }
        }
    });
    
    if($('#docs .more_docs').length>0){
        var id = $('#docs .more_docs').attr('id');
        if(isScrolledIntoWindow($('#'+id))){
            //getMoreDocs(id);
        }
    }


<scrd:loggedusers>
        $('.search_result').prepend('<input type="checkbox" style="float:right;" />');
        $('.search_result>input').click(function(){
            //refreshSelection(this);
            changeResSelection(this);
        });
        setScope('scope_multiple');
        $('#scope_single').hide();
</scrd:loggedusers>
    checkHeight(0);
});

    function changeResSelection(o){
        //alert("Checked: " + $(o).is(":checked"));
        var id =  $(getResultElement(o)).attr("id");
        var escapedId = id.substring(4).replace(/\//g,'-');
        if($(o).is(":checked")){
            var label = $(jq(id)+" div.resultText>a>b").html();
            addToContextMenuSelection(escapedId, label);
        }else{
            removeFromContextMenuSelection(escapedId);
        }
        
    }
    
    function toggleColumns(){
        
        $('.cols').toggle();
        setColumnsWidth();
    }
    
    function setColumnsWidth(){
        var margin = 
            parseInt($('.search_result:first').css("padding-left").replace("px", "")) +
            parseInt($('.search_result:first').css("padding-right").replace("px", ""));
        var w = $('#offset_0').width();
        if($('#cols2').is(':visible')){
            w = w - margin;
        }else{
            w = w / 2 - margin * 2;
        }
        $('.search_result').css('width', w);
        
    }

    function checkHeight(offset){
        var divs = $('#offset_'+offset+'>div.search_result').length;
        var left;
        var right;
        var max;
        for(var i=1; i<divs; i = i+2){
            left = $('#offset_'+offset+'>div.search_result')[i-1];
            right = $('#offset_'+offset+'>div.search_result')[i];
            checkRowHeight(left, right);
        }
    }
    
    function checkRowHeight(left, right){
        var max;
        var id1 = $(left).attr('id');
        if($(right).length>0){
            var id2 = $(right).attr('id');
            max = Math.max($(jq(id1)+'>div.result').height(), $(jq(id2)+'>div.result').height());
            //max = Math.max(max, $(jq(id2)+' img.th').height());
            max = Math.max(max, 140);
        }else{
            max = Math.max($(jq(id1)+'>div.result').height(), 140);
        }
        max = max + $(jq(id1)+'>div.collapse_label').height();
        $(left).css('height', max);
        $(right).css('height', max);
    }
    
    function getResultElement(el){
        var div = $(el);
        while(!$(div).hasClass('search_result') && $(div).attr("id")!="docs_content" && $(div).parent().length>0){
            div = $(div).parent();
        }
        return div;
    }
    
    function resultThumbLoaded(obj){
        checkRowHeightByElement(obj);
    }
    
    function checkRowHeightByElement(el){
        var div = getResultElement(el);
        if($(div).hasClass('0')){
            var div2 = $(div).prev();
            checkRowHeight(div2, div);
        }else{
            var div2 = $(div).next();
            checkRowHeight(div, div2);
        }
    }
    
    function getExtInfo(){
        $(".extInfo:hidden").each(function(){
            var info = $(this);
            //$(info).removeClass("extInfo");
            var pid_path = $(info).text();
            if(pid_path.indexOf("/")>0){
                var url =  "inc/results/extendedInfo.jsp?pid_path=" + pid_path;
                $.get(url, function(data) {
                    $(info).html(data);
                    $(info).show();
                    checkRowHeightByElement(info);
                });
            }
        });
    }

    function getMoreDocs(id){
        var offset = id.split('_')[1];
        var page = new PageQuery(window.location.search);
        page.setValue("offset", offset);
        var url =  "r.jsp?onlymore=true&" + page.toString();
        $.get(url, function(data) {
            $(jq(id)).html(data);
            $(jq(id)).removeClass('more_docs');
            getExtInfo();
            $('.loading_docs').hide();
            checkHeight(offset);
<scrd:loggedusers>
            $(jq(id)).append('<input type="checkbox" />');
</scrd:loggedusers>
        });
    }
    
    function sortByTitle(dir){
        $('#sort').val('title_cs '+dir);
        $('#searchForm').submit();
    }
    
    function sortByRank(){
        $('#sort').val('level asc, score desc');
        $('#searchForm').submit();
    }

    function addFilter(field, value){
        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        var f = "fq=" + field + ":\"" + value + "\"";
        if(window.location.search.indexOf(f)==-1){
            window.location = "r.jsp?" +
            page.toString() + "&" + f;
        }
    }

    function toggleCollapsed(root_pid, pid, offset){
        $(jq("res_"+root_pid)+" div.uncollapsed").toggle();
        $(jq('uimg_' + root_pid) ).toggleClass('uncollapseIcon');
        if($(jq("res_"+root_pid)+" div.uncollapsed").html()==""){
            uncollapse(root_pid, pid, offset);
        }
    }
    function uncollapse(root_pid, pid, offset){
          var page = new PageQuery(window.location.search);
          page.setValue("offset", offset);
          var url =  "inc/results/uncollapse.jsp?rows=10&" + page.toString() +
              "&type=uncollapse&collapsed=false&root_pid=" + root_pid +
              "&pid=" + pid +
              "&fq=root_pid:\"" + root_pid.split("_")[1] + "\"" + "&fq=-PID:\"" + pid + "\"";
          $.get(url, function(xml) {
              $(jq("res_"+root_pid)+" div.uncollapsed").html(xml);
              $(jq("res_"+root_pid)+" div.uncollapsed").scrollTop(0);
          });
    }
</script>