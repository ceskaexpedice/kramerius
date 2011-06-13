<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<div id="dateAxis" class="shadow box" style="float:right;right:0;z-index:2;background:white;position:absolute;">
<div id="showHideDA" ><a href="javascript:toggleDA();" title="show/hide date axis"><span class="ui-state-default ui-icon ui-icon-image ui-icon-circle-triangle-e"></span></a></div>
    <div id="daBox">
        <%@include file="../dateAxisV.jsp" %>
    </div>
</div>
<div id="facets">
    <%@include file="../facets.jsp" %>
</div>
<div id="docs">
    &#160;<c:out value="${numDocs}" />&#160;<c:out value="${numDocsStr}" />
    <%@include file="docs.jsp" %>
</div>
<script type="text/javascript">


$(document).ready(function(){
    $('.loading_docs').hide();
    
    $(document).bind('scroll', function(event){
        var id = $('#docs .more_docs').attr('id');
        if($('#docs .more_docs').length>0){
            if(isScrolledIntoView($('#'+id)), window){
                getMoreDocs(id);
            }
        }
    });
    if($('#docs .more_docs').length>0){
        var id = $('#docs .more_docs').attr('id');
        if(isScrolledIntoView($('#'+id)), window){
            getMoreDocs(id);
        }
    }


<%  if (request.getRemoteUser() != null) {%>
        $('.result').append('<input type="checkbox" />');
<% }%>
    checkHeight(0);
});
    

    function checkHeight(offset){
        
        var divs = $('#offset_'+offset+'>div.search_result').length;
        var left;
        var right;
        var max;
        for(var i=1; i<divs; i = i+2){
            left = $('#offset_'+offset+'>div.search_result')[i-1];
            right = $('#offset_'+offset+'>div.search_result')[i];
            max = Math.max($(left).height(), $(right).height());
            $(left).css('height', max);
            $(right).css('height', max);
        }
    }

    function getMoreDocs(id){
        var offset = id.split('_')[1];
        var page = new PageQuery(window.location.search);
        page.setValue("offset", offset);
        var url =  "r.jsp?onlymore=true&" + page.toString();
        $.get(url, function(data) {
            $('#'+id).html(data);
            $('#'+id).removeClass('more_docs');
            $('.loading_docs').hide();
            checkHeight(offset);
<%  if (request.getRemoteUser() != null) {%>
            $('#'+id+' .result').append('<input type="checkbox" />');
<% }%>
        });
    }

        function toggleFacet(facet){
            $('#facet_'+facet+' .moreFacets').toggle();
        }

        function toggleCollapsed(pid, offset){
            $("#res_"+pid+">div.uncollapsed").toggle();
            $('#uimg_' + pid ).toggleClass('uncollapseIcon');
            if($("#res_"+pid+">div.uncollapsed").html()==""){
                uncollapse(pid, offset);
            }
        }
        function uncollapse(pid, offset){
              var page = new PageQuery(window.location.search);
              page.setValue("offset", offset);
              var url =  "inc/results/uncollapse.jsp?rows=10&" + page.toString() +
                  "&type=uncollapse&collapsed=false&root_pid=" + pid + "&fq=root_pid:\"" + pid + "\"";
              $.get(url, function(xml) {
                  $("#res_"+pid+">div.uncollapsed").html(xml);
                  $("#res_"+pid+">div.uncollapsed").scrollTop(0);
              });
        }
</script>